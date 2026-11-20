package lab.cache;

/*
  Кеш. API схоже на Map, але це НЕ Map (і навмисно не наслідує java.util.Map):
  - ключі та значення не можуть бути null (NullPointerException);
  - записи можуть "протухати" (expiry) - після закінчення часу життя їх ніби немає;
  - записи можуть витіснятися (eviction), коли кеш переповнений;
  - тому get() може повернути null навіть для ключа, який щойно поклали.
 */
public interface Cache<K, V> {

    // значення або null, якщо його немає
    V get(K key);

    // кладе значення (замінює старе), може спричинити витіснення іншого запису
    void put(K key, V value);

    // кладе, тільки якщо живого значення ще немає; @return true, якщо поклали
    boolean putIfAbsent(K key, V value);

    // @return true, якщо живий запис був і його видалено
    boolean remove(K key);

    boolean containsKey(K key);

    // кількість живих записів */
    int size();

    void clear();

    CacheStats stats();
}
