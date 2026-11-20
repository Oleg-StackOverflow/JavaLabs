# Hard Task 2 — власні колекції та кеш

- `src/lab/collections/MyList.java` — інтерфейс зі списком методів з умови
- `src/lab/collections/MyArrayList.java` — список на масиві, реалізує `RandomAccess`
- `src/lab/collections/MyLinkedList.java` — двозв'язний список
- `src/lab/collections/MyLinkedHashSet.java` — власний LinkedHashSet (хеш-таблиця + порядок вставки)
- `src/lab/cache/` — кеш: інтерфейс `Cache`, реалізація `LruCache` (LRU-витіснення + TTL), статистика, слухач видалень
- `src/lab/Demo.java` — демонстрація
- `test/` — JUnit 4 тести (68 шт.), `lib/` — junit + hamcrest

Запуск тестів (JDK 17+):

    $cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
    javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
    java -cp $cp org.junit.runner.JUnitCore lab.collections.MyArrayListTest lab.collections.MyLinkedListTest lab.collections.MyLinkedHashSetTest lab.cache.LruCacheTest
    java -cp out/main lab.Demo 
    

