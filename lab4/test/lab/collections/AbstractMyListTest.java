package lab.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/*
  Спільні тести контракту MyList. Запускаються для обох реалізацій
  через підкласи MyArrayListTest і MyLinkedListTest.
 */
public abstract class AbstractMyListTest {

    // Фабрика списку будь-якого типу елементів - потрібна і для MyList<Object>, і для MyList<Integer>.
    protected abstract <T> MyList<T> newList();

    private MyList<Object> list;

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
        list.addAll((Object[]) null); // без приведення виклик неоднозначний: є 3 перевантаження addAll
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

    // Версія 2.0: generics і wildcards

    @Test
    public void typedGetNeedsNoCast() {
        MyList<String> s = newList();
        s.add("abc");
        String first = s.get(0);          // без (String)
        assertEquals(3, first.length());
    }

    @Test
    public void addAllMyListOfSubtype() {
        MyList<Number> numbers = newList();
        MyList<Integer> ints = newList();
        ints.addAll(new Integer[]{1, 2});
        MyList<Double> doubles = newList();
        doubles.add(0.5);

        assertTrue(numbers.addAll(ints));        // MyList<? extends Number> = MyList<Integer>
        assertTrue(numbers.addAll(0, doubles));  // MyList<? extends Number> = MyList<Double>
        assertFalse(numbers.addAll(newList()));  // порожній - нічого не змінилось
        assertArrayEquals(new Object[]{0.5, 1, 2}, numbers.toArray());
    }

    @Test
    public void addAllJavaCollectionOfSubtype() {
        MyList<Number> numbers = newList();
        assertTrue(numbers.addAll(List.of(1, 2, 3)));     // Collection<Integer>
        assertTrue(numbers.addAll(Set.of(4.0)));          // Collection<Double>
        assertEquals(4, numbers.size());
    }

    @Test
    public void addAllItselfDoublesList() {
        MyList<String> s = newList();
        s.addAll(new String[]{"a", "b"});
        s.addAll(s);
        assertArrayEquals(new Object[]{"a", "b", "a", "b"}, s.toArray());
        s.addAll(1, s);
        assertEquals(8, s.size());
        assertEquals("a", s.get(1));
    }

    @Test
    public void containsAllWithUnboundedWildcard() {
        list.addAll(new Object[]{"a", 1, 2.0});
        assertTrue(list.containsAll(List.of("a", 2.0)));
        assertFalse(list.containsAll(List.of("a", "zzz")));
        assertTrue(list.containsAll(List.of()));
    }

    @Test
    public void sortWithComparatorOfSupertype() {
        MyList<Integer> ints = newList();
        ints.addAll(new Integer[]{5, -1, 3, 0});
        Comparator<Number> byValue = Comparator.comparingDouble(Number::doubleValue);
        ints.sort(byValue);                               // Comparator<? super Integer>
        assertArrayEquals(new Object[]{-1, 0, 3, 5}, ints.toArray());
        Comparator<Object> byText = Comparator.comparing(Object::toString);
        ints.sort(byText.reversed());                     // Comparator<Object> теж підходить
        assertArrayEquals(new Object[]{5, 3, 0, -1}, ints.toArray());
    }

    @Test
    public void sortIsStable() {
        MyList<String> s = newList();
        s.addAll(new String[]{"bb", "a1", "cc", "a2", "dd", "a3"});
        s.sort(Comparator.comparingInt(String::length).thenComparing(x -> x.charAt(0)));
        assertArrayEquals(new Object[]{"a1", "a2", "a3", "bb", "cc", "dd"}, s.toArray());
    }

    @Test
    public void sortMatchesJavaSortOnRandomData() {
        Random rnd = new Random(1);
        MyList<Integer> mine = newList();
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            int v = rnd.nextInt(200);
            mine.add(v);
            expected.add(v);
        }
        mine.sort(Comparator.naturalOrder());
        expected.sort(Comparator.naturalOrder());
        assertArrayEquals(expected.toArray(), mine.toArray());
    }

    @Test
    public void removeIfWithPredicateOfSupertype() {
        MyList<Integer> ints = newList();
        ints.addAll(new Integer[]{1, 2, 3, 4, 5, 6});
        Predicate<Number> even = n -> n.intValue() % 2 == 0;
        assertTrue(ints.removeIf(even));
        assertArrayEquals(new Object[]{1, 3, 5}, ints.toArray());
        Predicate<Object> never = o -> false;
        assertFalse(ints.removeIf(never));
        assertTrue(ints.removeIf(o -> true));
        assertTrue(ints.isEmpty());
    }

    @Test
    public void toArrayTypedResizesAndFillsGivenArray() {
        MyList<String> s = newList();
        s.addAll(new String[]{"x", "y"});
        String[] small = s.toArray(new String[0]);
        assertEquals(String[].class, small.getClass());
        assertArrayEquals(new String[]{"x", "y"}, small);

        String[] big = {"1", "2", "3", "4"};
        String[] same = s.toArray(big);
        assertSame(big, same);
        assertArrayEquals(new String[]{"x", "y", null, "4"}, same); // як у java.util.List
    }

    @Test(expected = ArrayStoreException.class)
    public void toArrayWithWrongRuntimeTypeFails() {
        MyList<String> s = newList();
        s.add("x");
        s.toArray(new Integer[1]);
    }

    @Test
    public void forEachLoopAndIteratorRemove() {
        MyList<Integer> ints = newList();
        ints.addAll(new Integer[]{1, 2, 3, 4});
        int sum = 0;
        for (int x : ints) {
            sum += x;
        }
        assertEquals(10, sum);
        Iterator<Integer> it = ints.iterator();
        while (it.hasNext()) {
            if (it.next() > 2) it.remove();
        }
        assertArrayEquals(new Object[]{1, 2}, ints.toArray());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void iteratorIsFailFast() {
        list.addAll(new Object[]{1, 2, 3});
        for (Object o : list) {
            list.add(o);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void iteratorRemoveTwiceThrows() {
        list.add("a");
        Iterator<Object> it = list.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void iteratorPastEndThrows() {
        list.iterator().next();
    }
}
