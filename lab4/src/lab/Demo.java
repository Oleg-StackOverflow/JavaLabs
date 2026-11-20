package lab;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

import lab.cache.LruCache;
import lab.cache.RemovalListener;
import lab.collections.*;

// Демонстрація Custom Collections 2.0: generics + wildcards.
public class Demo {

    public static void main(String[] args) {
        System.out.println("=== 1. MyList<E>: типобезпечні списки ===");
        demoList(new MyArrayList<>());
        demoList(new MyLinkedList<>());

        System.out.println("\n=== 2. Wildcards у MyCollections (PECS) ===");
        MyList<Integer> ints = MyArrayList.of(5, 3, 8, 1);
        MyList<Double> doubles = MyLinkedList.of(2.5, 0.5);
        System.out.println("ints = " + ints + ", doubles = " + doubles);
        // sum(MyList<? extends Number>) приймає і Integer, і Double
        System.out.println("sum(ints) = " + MyCollections.sum(ints) + ", sum(doubles) = " + MyCollections.sum(doubles));

        // copy(MyList<? super T> dest, MyList<? extends T> src)
        MyList<Number> numbers = new MyArrayList<>();
        MyCollections.copy(numbers, ints);
        MyCollections.copy(numbers, doubles);
        System.out.println("copy у MyList<Number>: " + numbers);

        // addAll(Collection<? extends E>) - java.util.List<Integer> у MyList<Number>
        numbers.addAll(List.of(100, 200));
        System.out.println("numbers.addAll(List<Integer>): " + numbers);

        // sort(Comparator<? super E>) - один компаратор для Number підходить і для Integer
        Comparator<Number> byValue = Comparator.comparingDouble(Number::doubleValue);
        ints.sort(byValue);
        numbers.sort(byValue);
        System.out.println("ints.sort(Comparator<Number>): " + ints);
        System.out.println("numbers.sort(Comparator<Number>): " + numbers);

        System.out.println("max(ints) = " + MyCollections.max(ints)
                + ", max(numbers, Comparator<Number>) = " + MyCollections.max(numbers, byValue));

        // map(MyList<? extends T>, Function<? super T, ? extends R>)
        Function<Object, String> show = o -> "<" + o + ">";
        MyList<String> shown = MyCollections.map(ints, show);
        System.out.println("map(ints, Function<Object,String>): " + shown);

        // removeIf(Predicate<? super E>)
        Predicate<Number> big = n -> n.doubleValue() > 50;
        numbers.removeIf(big);
        System.out.println("numbers.removeIf(n > 50): " + numbers);
        System.out.println("describe(MyList<?>): " + MyCollections.describe(shown));

        System.out.println("\n=== 3. MyLinkedHashSet<E> ===");
        MyLinkedHashSet<Number> set = new MyLinkedHashSet<>();
        set.addAll(List.of(3, 1, 3, 2));          // Iterable<? extends Number> = List<Integer>
        set.addAll(MyLinkedList.of(1.5, 1.5));    // Iterable<? extends Number> = MyList<Double>
        System.out.println("set = " + set);
        Predicate<Object> isDouble = o -> o instanceof Double;
        set.removeIf(isDouble);                   // Predicate<? super Number> = Predicate<Object>
        System.out.println("set.removeIf(Predicate<Object>) = " + set);
        System.out.println("containsAll(List<Integer>) = " + set.containsAll(List.of(1, 2)));

        System.out.println("\n=== 4. Cache<K, V> ===");
        RemovalListener<Object, Object> logger =
                (k, v, cause) -> System.out.println("   [log] " + k + "=" + v + " -> " + cause);
        // RemovalListener<? super CharSequence, ? super Number> = RemovalListener<Object, Object>
        LruCache<CharSequence, Number> cache = new LruCache<>(3, LruCache.NO_EXPIRY, System::currentTimeMillis, logger);

        Map<String, Integer> prices = new TreeMap<>(Map.of("bread", 30, "milk", 45)); // TreeMap - стабільний порядок
        cache.putAll(prices);                     // Map<? extends CharSequence, ? extends Number>
        System.out.println("після putAll(Map<String,Integer>): " + cache);

        Function<CharSequence, Integer> length = CharSequence::length;
        System.out.println("computeIfAbsent(\"cheese\", length) = " + cache.computeIfAbsent("cheese", length));
        System.out.println("computeIfAbsent(\"bread\", length)  = " + cache.computeIfAbsent("bread", length) + " (взято з кешу)");
        cache.put("butter", 2.5);                 // переповнення -> витіснення LRU
        System.out.println("getAll(List<String>) = " + cache.getAll(List.of("bread", "milk", "cheese", "butter")));
        System.out.println("stats: " + cache.stats());
    }

    private static void demoList(MyList<String> list) {
        String name = list.getClass().getSimpleName();
        list.add("delta");
        list.add("alpha");
        list.add(1, "charlie");
        list.addAll(new String[]{"echo", "bravo"});
        System.out.println(name + "<String>: " + list + ", RandomAccess = " + (list instanceof RandomAccess));

        // Comparator<CharSequence> для MyList<String>: sort(Comparator<? super String>)
        Comparator<CharSequence> byLength = Comparator.comparingInt(CharSequence::length);
        list.sort(byLength.thenComparing(Object::toString));
        System.out.print("   sort(Comparator<CharSequence>): ");
        for (String s : list) {                   // for-each, бо MyList<E> extends Iterable<E>
            System.out.print(s.toUpperCase() + " "); // без приведення типу: get/next повертають String
        }
        System.out.println();
        String[] arr = list.toArray(new String[0]); // <T> T[] toArray(T[] a)
        System.out.println("   toArray(new String[0]) -> " + arr.getClass().getSimpleName() + ", length " + arr.length);
    }
}
