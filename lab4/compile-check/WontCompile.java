import lab.cache.LruCache;
import lab.collections.*;

import java.util.Comparator;
import java.util.List;

/**
 * Цей файл НАВМИСНО не компілюється. Кожен рядок з помилкою показує,
 * яку помилку generics ловлять ще на етапі компіляції (у версії 1.0 з Object
 * ці помилки "вилазили" лише під час виконання як ClassCastException).
 *
 * Перевірка: javac -cp out/main compile-check/WontCompile.java
 */
public class WontCompile {
    void examples() {
        MyList<Integer> ints = new MyArrayList<>();
        ints.add("five");                                        // 1: String у список Integer

        String s = ints.get(0);                                  // 2: Integer не String

        MyList<Number> numbers = ints;                           // 3: MyList<Integer> НЕ є MyList<Number>

        MyList<? extends Number> producer = ints;                // ok: читати можна...
        producer.add(1);                                         // 4: ...а записувати - ні

        MyCollections.copy(ints, MyArrayList.of(1.5));           // 5: Double не вкласти в MyList<Integer>

        Comparator<String> byLen = Comparator.comparingInt(String::length);
        ints.sort(byLen);                                        // 6: Comparator<String> не ? super Integer

        MyLinkedHashSet<Integer> set = new MyLinkedHashSet<>();
        set.addAll(List.of("a"));                                // 7: List<String> не Iterable<? extends Integer>

        LruCache<String, Integer> cache = new LruCache<>(10, 0);
        cache.computeIfAbsent("k", k -> "not a number");         // 8: loader має повертати ? extends Integer

        MyCollections.sum(MyArrayList.of("1", "2"));             // 9: String не Number
    }
}
