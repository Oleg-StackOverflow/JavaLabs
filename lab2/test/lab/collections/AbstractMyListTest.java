package lab.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/*
 Спільні тести контракту MyList. Запускаються для обох реалізацій
 через підкласи MyArrayListTest і MyLinkedListTest.
 */
public abstract class AbstractMyListTest {

    protected abstract MyList newList();

    private MyList list;

    @Before
    public void setUp() {
        list = newList();
    }

    @Test
    public void newListIsEmpty() {
        assertEquals(0, list.size());
        assertArrayEquals(new Object[0], list.toArray());
    }

    @Test
    public void addAppendsToEnd() {
        list.add("a");
        list.add("b");
        list.add("c");
        assertArrayEquals(new Object[]{"a", "b", "c"}, list.toArray());
    }

    @Test
    public void addByIndexInsertsAtStartMiddleAndEnd() {
        list.add("b");
        list.add(0, "a");   // початок
        list.add(2, "d");   // кінець (index == size)
        list.add(2, "c");   // середина
        assertArrayEquals(new Object[]{"a", "b", "c", "d"}, list.toArray());
    }

    @Test
    public void addAllAppendsArray() {
        list.add(1);
        list.addAll(new Object[]{2, 3, 4});
        assertArrayEquals(new Object[]{1, 2, 3, 4}, list.toArray());
    }

    @Test
    public void addAllByIndexInsertsInMiddle() {
        list.addAll(new Object[]{1, 5});
        list.addAll(1, new Object[]{2, 3, 4});
        assertArrayEquals(new Object[]{1, 2, 3, 4, 5}, list.toArray());
        list.addAll(0, new Object[]{0});
        list.addAll(list.size(), new Object[]{6});
        assertArrayEquals(new Object[]{0, 1, 2, 3, 4, 5, 6}, list.toArray());
    }

    @Test
    public void addAllEmptyArrayChangesNothing() {
        list.add("x");
        list.addAll(new Object[0]);
        assertEquals(1, list.size());
    }

    @Test
    public void getAndSet() {
        list.addAll(new Object[]{"a", "b", "c"});
        assertEquals("b", list.get(1));
        list.set(1, "B");
        assertEquals("B", list.get(1));
        assertEquals(3, list.size());
    }

    @Test
    public void removeReturnsElementAndShifts() {
        list.addAll(new Object[]{"a", "b", "c", "d"});
        assertEquals("a", list.remove(0));
        assertEquals("d", list.remove(2));
        assertEquals("b", list.remove(0));
        assertArrayEquals(new Object[]{"c"}, list.toArray());
        assertEquals("c", list.remove(0));
        assertEquals(0, list.size());
        list.add("again"); // після повного очищення список має бути робочим
        assertArrayEquals(new Object[]{"again"}, list.toArray());
    }

    @Test
    public void indexOfUsesEqualsAndSupportsNull() {
        list.addAll(new Object[]{"a", null, new String("b"), "b"});
        assertEquals(0, list.indexOf("a"));
        assertEquals(1, list.indexOf(null));
        assertEquals(2, list.indexOf("b"));   // перше входження, порівняння через equals
        assertEquals(-1, list.indexOf("zzz"));
    }

    @Test
    public void growsBeyondInitialCapacity() {
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        assertEquals(1000, list.size());
        assertEquals(999, list.get(999));
        assertEquals(500, list.get(500));
    }

    @Test
    public void toArrayReturnsCopy() {
        list.add("a");
        Object[] arr = list.toArray();
        arr[0] = "changed";
        assertEquals("a", list.get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOnEmptyThrows() {
        list.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getNegativeThrows() {
        list.add("a");
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addPastEndThrows() {
        list.add(1, "a");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setPastEndThrows() {
        list.add("a");
        list.set(1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void removePastEndThrows() {
        list.add("a");
        list.remove(1);
    }

    @Test(expected = NullPointerException.class)
    public void addAllNullThrows() {
        list.addAll(null);
    }

    /*
     Порівняння з java.util.ArrayList на 5000 випадкових операцій.
     Еталонний клас використовується ТІЛЬКИ в тесті як "оракул".
     */
    @Test
    public void randomOperationsMatchJavaArrayList() {
        Random rnd = new Random(42);
        List<Object> expected = new ArrayList<>();
        for (int step = 0; step < 5000; step++) {
            int op = rnd.nextInt(6);
            int v = rnd.nextInt(100);
            switch (op) {
                case 0 -> { list.add(v); expected.add(v); }
                case 1 -> {
                    int i = rnd.nextInt(expected.size() + 1);
                    list.add(i, v); expected.add(i, v);
                }
                case 2 -> {
                    int i = rnd.nextInt(expected.size() + 1);
                    Object[] chunk = {v, v + 1, v + 2};
                    list.addAll(i, chunk); expected.addAll(i, List.of(chunk));
                }
                case 3 -> {
                    if (!expected.isEmpty()) {
                        int i = rnd.nextInt(expected.size());
                        assertEquals(expected.remove(i), list.remove(i));
                    }
                }
                case 4 -> {
                    if (!expected.isEmpty()) {
                        int i = rnd.nextInt(expected.size());
                        list.set(i, v); expected.set(i, v);
                    }
                }
                default -> assertEquals(expected.indexOf(v), list.indexOf(v));
            }
            assertEquals(expected.size(), list.size());
        }
        assertArrayEquals(expected.toArray(), list.toArray());
    }
}
