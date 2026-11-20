package lab.cache;

import java.util.Map;
import java.util.function.Function;

/*
  Кеш. API схоже на Map, але це НЕ Map (і навмисно не наслідує java.util.Map):
   - ключі та значення не можуть бути null (NullPointerException);
   - записи можуть "протухати" (expiry) - після закінчення часу життя їх ніби немає;
   - записи можуть витіснятися (eviction), коли кеш переповнений;
   - тому get() може повернути null навіть для ключа, який щойно поклали.
 */
public interface Cache<K, V> {

    // значення або null, якщо його немає, воно протухло чи було витіснене 
    V get(K key);

    // кладе значення (замінює старе), може спричинити витіснення іншого запису 
    void put(K key, V value);

    // кладе, тільки якщо живого значення ще немає; @return true, якщо поклали 
    boolean putIfAbsent(K key, V value);

    // @return true, якщо живий запис був і його видалено 
    boolean remove(K key);

    boolean containsKey(K key);

    // кількість живих записів 
    int size();

    void clear();

    CacheStats stats();

    // ---------- версія 2.0: wildcards ----------

    // Кладе всі пари з map. Map<String, Integer> підходить для Cache<CharSequence, Number>.
    void putAll(Map<? extends K, ? extends V> map);

    // Знайдені живі значення для переданих ключів (відсутніх у результаті немає).
    Map<K, V> getAll(Iterable<? extends K> keys);

    /*
      Значення з кешу або обчислене loader-ом і покладене в кеш.
      loader приймає щонайменше K (? super K) і повертає щонайбільше V (? extends V).
      Якщо loader повернув null - нічого не кладемо і повертаємо null.
     */
    V computeIfAbsent(K key, Function<? super K, ? extends V> loader);
}
