package lab.collections;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/*
  Власна реалізація LinkedHashSet без використання HashMap/HashSet.
  Всередині дві структури, які ділять одні й ті самі вузли:
  1) хеш-таблиця з ланцюжками (масив "кошиків") - для пошуку за O(1);
  2) двозв'язний список before/after - щоб пам'ятати порядок вставки.
 */
public class MyLinkedHashSet<E> implements Iterable<E> {

    private static final int DEFAULT_CAPACITY = 16;       // завжди степінь двійки
    private static final float LOAD_FACTOR = 0.75f;

    private static class Entry<E> {
        final E value;
        final int hash;
        Entry<E> nextInBucket;   // наступний у тому ж кошику
        Entry<E> before, after;  // сусіди в порядку вставки

        Entry(E value, int hash) {
            this.value = value;
            this.hash = hash;
        }
    }

    private Entry<E>[] table;
    private Entry<E> head;   // найстаріший
    private Entry<E> tail;   // найновіший
    private int size;
    private int threshold;
    private int modCount;    // для fail-fast ітератора

    public MyLinkedHashSet() {
        table = newTable(DEFAULT_CAPACITY);
        threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    // @return true, якщо елемента ще не було і його додано
    public boolean add(E e) {
        int h = hash(e);
        int i = indexFor(h, table.length);
        for (Entry<E> x = table[i]; x != null; x = x.nextInBucket) {
            if (x.hash == h && eq(x.value, e)) {
                return false; // вже є - порядок НЕ змінюється (як у LinkedHashSet)
            }
        }
        Entry<E> n = new Entry<>(e, h);
        n.nextInBucket = table[i];
        table[i] = n;
        linkLast(n);
        size++;
        modCount++;
        if (size > threshold) {
            resize();
        }
        return true;
    }

    public boolean contains(Object o) {
        return find(o) != null;
    }

    public boolean remove(Object o) {
        int h = hash(o);
        int i = indexFor(h, table.length);
        Entry<E> prev = null;
        for (Entry<E> x = table[i]; x != null; prev = x, x = x.nextInBucket) {
            if (x.hash == h && eq(x.value, o)) {
                if (prev == null) {
                    table[i] = x.nextInBucket;
                } else {
                    prev.nextInBucket = x.nextInBucket;
                }
                unlink(x);
                size--;
                modCount++;
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        table = newTable(table.length);
        head = tail = null;
        size = 0;
        modCount++;
    }

    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (Entry<E> x = head; x != null; x = x.after) {
            arr[i++] = x.value;
        }
        return arr;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private Entry<E> next = head;
            private Entry<E> lastReturned;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (next == null) {
                    throw new NoSuchElementException();
                }
                lastReturned = next;
                next = next.after;
                return lastReturned.value;
            }

            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                MyLinkedHashSet.this.remove(lastReturned.value);
                lastReturned = null;
                expectedModCount = modCount;
            }
        };
    }

    // ---------- допоміжне ----------

    private Entry<E> find(Object o) {
        int h = hash(o);
        for (Entry<E> x = table[indexFor(h, table.length)]; x != null; x = x.nextInBucket) {
            if (x.hash == h && eq(x.value, o)) {
                return x;
            }
        }
        return null;
    }

    // Подвоюємо таблицю і перекидаємо вузли. Порядок вставки при цьому не чіпаємо.
    private void resize() {
        Entry<E>[] newTab = newTable(table.length * 2);
        // йдемо по списку вставки - так простіше, ніж по старих кошиках
        for (Entry<E> x = head; x != null; x = x.after) {
            int i = indexFor(x.hash, newTab.length);
            x.nextInBucket = newTab[i];
            newTab[i] = x;
        }
        table = newTab;
        threshold = (int) (newTab.length * LOAD_FACTOR);
    }

    private void linkLast(Entry<E> n) {
        if (tail == null) {
            head = tail = n;
        } else {
            tail.after = n;
            n.before = tail;
            tail = n;
        }
    }

    private void unlink(Entry<E> x) {
        if (x.before == null) head = x.after; else x.before.after = x.after;
        if (x.after == null) tail = x.before; else x.after.before = x.before;
        x.before = x.after = null;
    }

    // null дозволений (як у java.util.LinkedHashSet), його хеш = 0 
    private static int hash(Object o) {
        if (o == null) return 0;
        int h = o.hashCode();
        return h ^ (h >>> 16); // "перемішуємо" старші біти, бо індекс береться з молодших
    }

    private static int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    private static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    @SuppressWarnings("unchecked")
    private static <E> Entry<E>[] newTable(int n) {
        return (Entry<E>[]) new Entry[n];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Entry<E> x = head; x != null; x = x.after) {
            sb.append(x.value);
            if (x.after != null) sb.append(", ");
        }
        return sb.append(']').toString();
    }
}
