package lab.cache;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LruCacheTest {

    // "Ручний" годинник: час іде тільки коли ми кажемо - тести не залежать від sleep().
    private long now;
    private final List<String> removals = new ArrayList<>();

    @Before
    public void setUp() {
        now = 1_000;
        removals.clear();
    }

    private LruCache<String, Integer> cache(int maxSize, long ttl) {
        return new LruCache<>(maxSize, ttl, () -> now,
                (k, v, cause) -> removals.add(k + ":" + cause));
    }

    // ---------- схоже на Map ----------

    @Test
    public void putAndGet() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        c.put("a", 1);
        assertEquals(Integer.valueOf(1), c.get("a"));
        assertNull(c.get("b"));
    }

    @Test
    public void oneValuePerKey() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        c.put("a", 1);
        c.put("a", 2);
        assertEquals(Integer.valueOf(2), c.get("a"));
        assertEquals(1, c.size());
        assertEquals(List.of("a:REPLACED"), removals);
    }

    @Test
    public void putIfAbsentAndRemove() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        assertTrue(c.putIfAbsent("a", 1));
        assertFalse(c.putIfAbsent("a", 2));
        assertEquals(Integer.valueOf(1), c.get("a"));
        assertTrue(c.remove("a"));
        assertFalse(c.remove("a"));
        assertFalse(c.containsKey("a"));
    }

    // ---------- НЕ схоже на Map: null заборонений ----------

    @Test(expected = NullPointerException.class)
    public void nullKeyInPutThrows() {
        cache(10, 0).put(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void nullValueThrows() {
        cache(10, 0).put("a", null);
    }

    @Test(expected = NullPointerException.class)
    public void nullKeyInGetThrows() {
        cache(10, 0).get(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullKeyInContainsKeyThrows() {
        cache(10, 0).containsKey(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullKeyInRemoveThrows() {
        cache(10, 0).remove(null);
    }

    // ---------- НЕ схоже на Map: expiry ----------

    @Test
    public void entryExpiresAfterTtl() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 99;
        assertEquals(Integer.valueOf(1), c.get("a"));
        now += 1; // рівно ttl
        assertNull(c.get("a"));
        assertFalse(c.containsKey("a"));
        assertEquals(List.of("a:EXPIRED"), removals);
        assertEquals(1, c.stats().expirations());
    }

    @Test
    public void putRefreshesTtl() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 80;
        c.put("a", 2);
        now += 80; // 160 від першого put, але 80 від другого
        assertEquals(Integer.valueOf(2), c.get("a"));
    }

    @Test
    public void getDoesNotRefreshTtl() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 60;
        c.get("a");
        now += 60;
        assertNull(c.get("a")); // expire-after-WRITE
    }

    @Test
    public void sizeCountsOnlyLiveEntries() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 50;
        c.put("b", 2);
        now += 60; // "a" протух, "b" ще ні
        assertEquals(1, c.size());
    }

    @Test
    public void putIfAbsentReplacesExpired() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 100;
        assertTrue(c.putIfAbsent("a", 2));
        assertEquals(Integer.valueOf(2), c.get("a"));
    }

    // ---------- НЕ схоже на Map: eviction ----------

    @Test
    public void evictsLeastRecentlyUsed() {
        LruCache<String, Integer> c = cache(3, LruCache.NO_EXPIRY);
        c.put("a", 1);
        c.put("b", 2);
        c.put("c", 3);
        c.get("a");      // тепер найстаріший за використанням - "b"
        c.put("d", 4);   // переповнення
        assertNull(c.get("b"));
        assertNotNull(c.get("a"));
        assertNotNull(c.get("c"));
        assertNotNull(c.get("d"));
        assertEquals(3, c.size());
        assertEquals(List.of("b:EVICTED"), removals);
        assertEquals(1, c.stats().evictions());
    }

    @Test
    public void containsKeyDoesNotAffectLruOrder() {
        LruCache<String, Integer> c = cache(2, LruCache.NO_EXPIRY);
        c.put("a", 1);
        c.put("b", 2);
        c.containsKey("a");
        c.put("c", 3);
        assertFalse(c.containsKey("a"));
    }

    @Test
    public void expiredEntriesAreRemovedBeforeEvictingLiveOnes() {
        LruCache<String, Integer> c = cache(2, 100);
        c.put("old", 1);
        now += 50;
        c.put("b", 2);
        c.get("old");    // "old" найсвіжіший за LRU, але скоро протухне
        now += 60;       // "old" протух (110), "b" живий (60)
        c.put("c", 3);   // місце звільняється за рахунок протухлого, а не "b"
        assertNotNull(c.get("b"));
        assertNotNull(c.get("c"));
        assertEquals(List.of("old:EXPIRED"), removals);
        assertEquals(0, c.stats().evictions());
    }

    // ---------- статистика ----------

    @Test
    public void statsCountHitsAndMisses() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        c.put("a", 1);
        c.get("a");
        c.get("a");
        c.get("x");
        CacheStats s = c.stats();
        assertEquals(2, s.hits());
        assertEquals(1, s.misses());
        assertEquals(2.0 / 3, s.hitRate(), 1e-9);
    }

    @Test
    public void clearNotifiesListener() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        c.put("a", 1);
        c.put("b", 2);
        c.clear();
        assertEquals(0, c.size());
        assertEquals(2, removals.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroMaxSizeRejected() {
        new LruCache<String, Integer>(0, 0);
    }

    // ---------- версія 2.0: wildcards ----------

    @Test
    public void putAllFromMapOfSubtypes() {
        LruCache<CharSequence, Number> c = new LruCache<>(10, LruCache.NO_EXPIRY);
        java.util.Map<String, Integer> m = new java.util.TreeMap<>(java.util.Map.of("a", 1, "b", 2));
        c.putAll(m);                                   // Map<? extends CharSequence, ? extends Number>
        assertEquals(2, c.size());
        assertEquals(1, c.get("a"));
    }

    @Test
    public void putAllWithNullValueInsertsNothing() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        m.put("ok", 1);
        m.put("bad", null);
        try {
            c.putAll(m);
            fail("очікувався NullPointerException");
        } catch (NullPointerException expected) {
            // ok
        }
        assertEquals(0, c.size());                     // "все або нічого"
    }

    @Test
    public void getAllReturnsOnlyLiveEntries() {
        LruCache<String, Integer> c = cache(10, 100);
        c.put("a", 1);
        now += 50;
        c.put("b", 2);
        now += 60;                                     // "a" протух
        java.util.Map<String, Integer> r = c.getAll(java.util.List.of("a", "b", "zzz"));
        assertEquals(java.util.Map.of("b", 2), r);
    }

    @Test
    public void computeIfAbsentCallsLoaderOnlyOnMiss() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        int[] calls = {0};
        java.util.function.Function<CharSequence, Integer> loader = k -> {
            calls[0]++;
            return k.length();
        };
        assertEquals(Integer.valueOf(5), c.computeIfAbsent("hello", loader));  // Function<? super String, ...>
        assertEquals(Integer.valueOf(5), c.computeIfAbsent("hello", loader));
        assertEquals(1, calls[0]);
    }

    @Test
    public void computeIfAbsentWithNullResultStoresNothing() {
        LruCache<String, Integer> c = cache(10, LruCache.NO_EXPIRY);
        assertNull(c.computeIfAbsent("x", k -> null));
        assertFalse(c.containsKey("x"));
    }

    @Test
    public void listenerOfSupertypesIsAccepted() {
        java.util.List<Object> log = new ArrayList<>();
        RemovalListener<Object, Object> generic = (k, v, cause) -> log.add(cause);
        LruCache<String, Integer> c = new LruCache<>(1, LruCache.NO_EXPIRY, () -> now, generic);
        c.put("a", 1);
        c.put("b", 2);                                 // "a" витіснено
        assertEquals(java.util.List.of(RemovalCause.EVICTED), log);
    }
}
