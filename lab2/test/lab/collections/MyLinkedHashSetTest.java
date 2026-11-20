package lab.collections;

import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;

import static org.junit.Assert.*;

public class MyLinkedHashSetTest {

    @Test
    public void keepsInsertionOrder() {
        MyLinkedHashSet<String> set = new MyLinkedHashSet<>();
        set.add("c");
        set.add("a");
        set.add("b");
        assertArrayEquals(new Object[]{"c", "a", "b"}, set.toArray());
    }

    @Test
    public void duplicatesAreIgnoredAndDoNotMoveElement() {
        MyLinkedHashSet<String> set = new MyLinkedHashSet<>();
        assertTrue(set.add("a"));
        assertTrue(set.add("b"));
        assertFalse(set.add("a"));
        assertEquals(2, set.size());
        assertArrayEquals(new Object[]{"a", "b"}, set.toArray());
    }

    @Test
    public void removeKeepsOrderOfOthers() {
        MyLinkedHashSet<Integer> set = new MyLinkedHashSet<>();
        for (int i = 1; i <= 5; i++) set.add(i);
        assertTrue(set.remove(3));
        assertFalse(set.remove(3));
        assertFalse(set.contains(3));
        assertArrayEquals(new Object[]{1, 2, 4, 5}, set.toArray());
        set.add(3); // повторне додавання - в кінець
        assertArrayEquals(new Object[]{1, 2, 4, 5, 3}, set.toArray());
    }

    @Test
    public void supportsNull() {
        MyLinkedHashSet<String> set = new MyLinkedHashSet<>();
        assertTrue(set.add(null));
        assertFalse(set.add(null));
        assertTrue(set.contains(null));
        assertTrue(set.remove(null));
        assertTrue(set.isEmpty());
    }

    // Ключ з однаковим хешем - перевіряємо, що колізії в одному кошику обробляються
    private record BadHash(int id) {
        @Override
        public int hashCode() {
            return 7;
        }
    }

    @Test
    public void handlesHashCollisions() {
        MyLinkedHashSet<BadHash> set = new MyLinkedHashSet<>();
        for (int i = 0; i < 50; i++) set.add(new BadHash(i));
        assertEquals(50, set.size());
        assertTrue(set.contains(new BadHash(25)));
        assertTrue(set.remove(new BadHash(0)));   // голова ланцюжка/списку
        assertTrue(set.remove(new BadHash(49)));  // хвіст
        assertTrue(set.remove(new BadHash(25)));  // середина
        assertEquals(47, set.size());
        assertFalse(set.contains(new BadHash(25)));
    }

    @Test
    public void resizeKeepsAllElementsAndOrder() {
        MyLinkedHashSet<Integer> set = new MyLinkedHashSet<>();
        for (int i = 999; i >= 0; i--) set.add(i);
        assertEquals(1000, set.size());
        Object[] arr = set.toArray();
        assertEquals(999, arr[0]);
        assertEquals(0, arr[999]);
        for (int i = 0; i < 1000; i++) assertTrue(set.contains(i));
    }

    @Test
    public void iteratorRemove() {
        MyLinkedHashSet<Integer> set = new MyLinkedHashSet<>();
        for (int i = 0; i < 10; i++) set.add(i);
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) it.remove();
        }
        assertArrayEquals(new Object[]{1, 3, 5, 7, 9}, set.toArray());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void iteratorIsFailFast() {
        MyLinkedHashSet<Integer> set = new MyLinkedHashSet<>();
        set.add(1);
        set.add(2);
        for (Integer x : set) {
            set.add(x + 10);
        }
    }

    @Test
    public void clearEmptiesSet() {
        MyLinkedHashSet<String> set = new MyLinkedHashSet<>();
        set.add("a");
        set.clear();
        assertEquals(0, set.size());
        assertFalse(set.contains("a"));
        set.add("b");
        assertArrayEquals(new Object[]{"b"}, set.toArray());
    }

    @Test
    public void randomOperationsMatchJavaLinkedHashSet() {
        Random rnd = new Random(7);
        MyLinkedHashSet<Integer> mine = new MyLinkedHashSet<>();
        LinkedHashSet<Integer> expected = new LinkedHashSet<>();
        for (int step = 0; step < 20000; step++) {
            int v = rnd.nextInt(500);
            switch (rnd.nextInt(3)) {
                case 0 -> assertEquals(expected.add(v), mine.add(v));
                case 1 -> assertEquals(expected.remove(v), mine.remove(v));
                default -> assertEquals(expected.contains(v), mine.contains(v));
            }
        }
        assertEquals(expected.size(), mine.size());
        assertArrayEquals(expected.toArray(), mine.toArray());
    }
}
