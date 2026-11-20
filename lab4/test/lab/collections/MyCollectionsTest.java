package lab.collections;

import org.junit.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;

// Утиліти з wildcards: перевіряємо, що вони приймають "сусідні" типи і працюють правильно
public class MyCollectionsTest {

    @Test
    public void copyFromSubtypeIntoSupertype() {
        MyList<Integer> src = MyArrayList.of(1, 2);
        MyList<Object> dest = MyLinkedList.of("start");
        MyCollections.copy(dest, src);           // dest: ? super Integer, src: ? extends Integer
        assertArrayEquals(new Object[]{"start", 1, 2}, dest.toArray());
    }

    @Test
    public void copyWithExplicitTypeArgument() {
        MyList<Integer> ints = MyArrayList.of(7);
        MyList<Number> nums = new MyArrayList<>();
        MyCollections.<Number>copy(nums, ints);  // T = Number: dest MyList<Number>, src MyList<Integer>
        assertEquals(7, nums.get(0));
    }

    @Test
    public void sumAcceptsAnyNumberList() {
        assertEquals(6.0, MyCollections.sum(MyArrayList.of(1, 2, 3)), 1e-9);
        assertEquals(1.5, MyCollections.sum(MyLinkedList.of(1.0, 0.5)), 1e-9);
        assertEquals(10.0, MyCollections.sum(MyArrayList.<Number>of(5, 2.5, 2.5f)), 1e-9);
        assertEquals(0.0, MyCollections.sum(new MyArrayList<Long>()), 1e-9);
    }

    @Test
    public void maxNaturalOrderAndComparator() {
        assertEquals("pear", MyCollections.max(MyArrayList.of("apple", "pear", "fig")));
        Comparator<CharSequence> byLength = Comparator.comparingInt(CharSequence::length);
        assertEquals("apple", MyCollections.max(MyLinkedList.of("apple", "pear", "fig"), byLength));
    }

    // Клас, що порівнюється через батька: Comparable<? super T> у max() дозволяє це
    static class Animal implements Comparable<Animal> {
        final int weight;
        Animal(int weight) { this.weight = weight; }
        @Override public int compareTo(Animal o) { return Integer.compare(weight, o.weight); }
    }

    static class Cat extends Animal {
        Cat(int weight) { super(weight); }
    }

    @Test
    public void maxWorksForSubclassOfComparable() {
        Cat big = new Cat(9);
        MyList<Cat> cats = MyArrayList.of(new Cat(3), big, new Cat(5));
        Cat max = MyCollections.max(cats);       // Cat implements Comparable<Animal>, не Comparable<Cat>
        assertSame(big, max);
    }

    @Test(expected = NoSuchElementException.class)
    public void maxOfEmptyThrows() {
        MyCollections.max(new MyArrayList<Integer>());
    }

    @Test
    public void mapWithWiderFunction() {
        Function<Object, Integer> hash = Object::hashCode;
        MyList<Integer> r = MyCollections.map(MyArrayList.of("a", "b"), hash);
        assertArrayEquals(new Object[]{"a".hashCode(), "b".hashCode()}, r.toArray());
        Function<Number, Integer> round = n -> (int) Math.round(n.doubleValue());
        MyList<Number> rounded = MyCollections.<Double, Number>map(MyLinkedList.of(1.4, 2.6), round);
        assertArrayEquals(new Object[]{1, 3}, rounded.toArray());
    }

    @Test
    public void filterWithPredicateOfSupertype() {
        Predicate<Object> notNull = o -> o != null;
        MyList<String> r = MyCollections.filter(MyArrayList.of("a", null, "b"), notNull);
        assertArrayEquals(new Object[]{"a", "b"}, r.toArray());
    }

    @Test
    public void fillIntoSupertypeList() {
        MyList<Object> any = new MyLinkedList<>();
        MyCollections.fill(any, 3, "x");
        assertArrayEquals(new Object[]{"x", "x", "x"}, any.toArray());
    }

    @Test
    public void disjointAndDescribeAcceptAnyLists() {
        assertTrue(MyCollections.disjoint(MyArrayList.of(1, 2), MyLinkedList.of("1", "2")));
        assertFalse(MyCollections.disjoint(MyArrayList.of(1, 2), MyLinkedList.<Object>of("x", 2)));
        assertEquals("MyLinkedList (2 ел.): [a, b]", MyCollections.describe(MyLinkedList.of("a", "b")));
    }
}
