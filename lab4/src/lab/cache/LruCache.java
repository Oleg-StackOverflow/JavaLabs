package lab.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongSupplier;

/*
 Кеш з двома політиками:
  - eviction: LRU (Least Recently Used) - при переповненні викидається запис,
    до якого найдовше не звертались;
  - expiry: expire-after-write - запис живе ttlMillis з моменту останнього put().
 Пошук - через HashMap (ключ -> вузол), порядок використання - власний
 двозв'язний список: голова = найсвіжіший, хвіст = кандидат на витіснення.
 Усі операції O(1). Методи synchronized, щоб кеш можна було ділити між потоками.
 */
public class LruCache<K, V> implements Cache<K, V> {

    public static final long NO_EXPIRY = 0;

    private static final class Node<K, V> {
        final K key;
        V value;
        long expiresAt;          // мітка часу, коли запис протухає (або Long.MAX_VALUE)
        Node<K, V> prev, next;

        Node(K key, V value, long expiresAt) {
            this.key = key;
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    private final int maxSize;
    private final long ttlMillis;
    private final LongSupplier clock;               // підміняється в тестах
    private final RemovalListener<? super K, ? super V> listener;

    private final Map<K, Node<K, V>> index = new HashMap<>();
    private Node<K, V> head;  // most recently used
    private Node<K, V> tail;  // least recently used

    private long hits, misses, evictions, expirations;

    public LruCache(int maxSize, long ttlMillis) {
        this(maxSize, ttlMillis, System::currentTimeMillis, null);
    }

    /*
     listener типу RemovalListener<? super K, ? super V>: один слухач
     RemovalListener<Object, Object> (наприклад, логер) підходить для будь-якого кешу.
     */
    public LruCache(int maxSize, long ttlMillis, LongSupplier clock, RemovalListener<? super K, ? super V> listener) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize має бути > 0");
        }
        if (ttlMillis < 0) {
            throw new IllegalArgumentException("ttlMillis не може бути від'ємним");
        }
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
        this.clock = Objects.requireNonNull(clock);
        this.listener = listener;
    }

    @Override
    public synchronized V get(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> n = index.get(key);
        if (n == null) {
            misses++;
            return null;
        }
        if (isExpired(n)) {
            expire(n);
            misses++;
            return null;
        }
        moveToHead(n);
        hits++;
        return n.value;
    }

    @Override
    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> n = index.get(key);
        if (n != null) {
            V old = n.value;
            n.value = value;
            n.expiresAt = newExpiry();
            moveToHead(n);
            notify(key, old, RemovalCause.REPLACED);
            return;
        }
        n = new Node<>(key, value, newExpiry());
        index.put(key, n);
        addToHead(n);
        if (index.size() > maxSize) {
            // спершу пробуємо звільнити місце за рахунок протухлих - це "чесніше",
            // ніж викидати живий запис
            purgeExpired();
            if (index.size() > maxSize) {
                evict(tail);
            }
        }
    }

    @Override
    public synchronized boolean putIfAbsent(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Node<K, V> n = index.get(key);
        if (n != null && !isExpired(n)) {
            return false;
        }
        put(key, value);
        return true;
    }

    @Override
    public synchronized boolean remove(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> n = index.get(key);
        if (n == null) {
            return false;
        }
        if (isExpired(n)) {
            expire(n);
            return false;
        }
        unlinkAndForget(n);
        notify(n.key, n.value, RemovalCause.EXPLICIT);
        return true;
    }

    @Override
    public synchronized boolean containsKey(K key) {
        Objects.requireNonNull(key, "key");
        Node<K, V> n = index.get(key);
        if (n == null) {
            return false;
        }
        if (isExpired(n)) {
            expire(n);
            return false;
        }
        return true; // containsKey не рахується як "використання" і не міняє LRU-порядок
    }

    @Override
    public synchronized int size() {
        purgeExpired();
        return index.size();
    }

    @Override
    public synchronized void clear() {
        for (Node<K, V> n = head; n != null; n = n.next) {
            notify(n.key, n.value, RemovalCause.EXPLICIT);
        }
        index.clear();
        head = tail = null;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map");
        // спершу перевіряємо все, щоб не покласти половину і впасти на null посередині
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            Objects.requireNonNull(e.getKey(), "key");
            Objects.requireNonNull(e.getValue(), "value");
        }
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public synchronized Map<K, V> getAll(Iterable<? extends K> keys) {
        Objects.requireNonNull(keys, "keys");
        Map<K, V> result = new LinkedHashMap<>();
        for (K k : keys) {
            V v = get(k);
            if (v != null) {
                result.put(k, v);
            }
        }
        return result;
    }

    @Override
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> loader) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(loader, "loader");
        V v = get(key);
        if (v != null) {
            return v;
        }
        V loaded = loader.apply(key);
        if (loaded != null) {
            put(key, loaded);
        }
        return loaded;
    }

    @Override
    public synchronized CacheStats stats() {
        return new CacheStats(hits, misses, evictions, expirations);
    }

    // Ключі від найсвіжішого до найстарішого - зручно для налагодження і тестів.
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("LruCache[");
        for (Node<K, V> n = head; n != null; n = n.next) {
            sb.append(n.key).append('=').append(n.value);
            if (n.next != null) sb.append(", ");
        }
        return sb.append(']').toString();
    }

    // ---------- expiry / eviction ----------

    private long newExpiry() {
        return ttlMillis == NO_EXPIRY ? Long.MAX_VALUE : clock.getAsLong() + ttlMillis;
    }

    private boolean isExpired(Node<K, V> n) {
        return clock.getAsLong() >= n.expiresAt;
    }

    private void expire(Node<K, V> n) {
        unlinkAndForget(n);
        expirations++;
        notify(n.key, n.value, RemovalCause.EXPIRED);
    }

    private void evict(Node<K, V> n) {
        unlinkAndForget(n);
        evictions++;
        notify(n.key, n.value, RemovalCause.EVICTED);
    }

    private void purgeExpired() {
        if (ttlMillis == NO_EXPIRY) {
            return;
        }
        Node<K, V> n = head;
        while (n != null) {
            Node<K, V> next = n.next;
            if (isExpired(n)) {
                expire(n);
            }
            n = next;
        }
    }

    private void notify(K key, V value, RemovalCause cause) {
        if (listener != null) {
            listener.onRemoval(key, value, cause);
        }
    }

    // ---------- двозв'язний список ----------

    private void addToHead(Node<K, V> n) {
        n.prev = null;
        n.next = head;
        if (head != null) head.prev = n;
        head = n;
        if (tail == null) tail = n;
    }

    private void unlink(Node<K, V> n) {
        if (n.prev == null) head = n.next; else n.prev.next = n.next;
        if (n.next == null) tail = n.prev; else n.next.prev = n.prev;
        n.prev = n.next = null;
    }

    private void moveToHead(Node<K, V> n) {
        if (n != head) {
            unlink(n);
            addToHead(n);
        }
    }

    private void unlinkAndForget(Node<K, V> n) {
        unlink(n);
        index.remove(n.key);
    }
}
