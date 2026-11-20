# Hard Task 4 — Custom Collections 2.0 (generics + wildcards)

Продовження Hard Task 2: усі колекції стали узагальненими.

## Структура

- `src/lab/collections/MyList.java` — `MyList<E> extends Iterable<E>`; нові методи з wildcards:
  `addAll(Collection<? extends E>)`, `addAll(MyList<? extends E>)`, `containsAll(Collection<?>)`,
  `sort(Comparator<? super E>)`, `removeIf(Predicate<? super E>)`, `<T> T[] toArray(T[])`
- `MyArrayList<E>`, `MyLinkedList<E>` — реалізації + fail-fast ітератори + сортування злиттям (`Sorting`)
- `MyCollections` — утиліти за правилом PECS: `copy`, `sum`, `max`, `map`, `filter`, `fill`, `disjoint`, `describe`
- `MyLinkedHashSet<E>` — `addAll(Iterable<? extends E>)`, `containsAll/removeAll(Iterable<?>)`, `removeIf(Predicate<? super E>)`
- `src/lab/cache/` — `putAll(Map<? extends K, ? extends V>)`, `getAll(Iterable<? extends K>)`,
  `computeIfAbsent(K, Function<? super K, ? extends V>)`, слухач `RemovalListener<? super K, ? super V>`
- `src/lab/Demo.java` — демонстрація
- `compile-check/WontCompile.java` — 9 прикладів коду, який generics не дають скомпілювати
  (`expected-errors.txt` — вивід javac)
- `test/lab` — JUnit 4 тести, `lib/` — junit + hamcrest

## Запуск (Windows, PowerShell)

Потрібен JDK 17+. Усі команди виконувати з кореня проєкту (`lab4`).

Компіляція:

```powershell
javac -encoding UTF-8 -Xlint:all -Werror -d out/main (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Демо:

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main lab.Demo
```

## Тести

Очікуваний результат: `OK (117 tests)`.

```powershell
$cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
java -cp $cp org.junit.runner.JUnitCore lab.collections.MyArrayListTest lab.collections.MyLinkedListTest lab.collections.MyLinkedHashSetTest lab.collections.MyCollectionsTest lab.cache.LruCacheTest
```

`AbstractMyListTest` він абстрактний, його тести запускаються через `MyArrayListTest` і `MyLinkedListTest`.

## Перевірка помилок компіляції

Файл навмисно не компілюється: javac має видати рівно `9 errors`.

```powershell
javac -encoding UTF-8 -cp out/main -d out/wont-compile compile-check/WontCompile.java 2>&1 | Out-File -Encoding utf8 compile-check/expected-errors.txt
Get-Content compile-check/expected-errors.txt -Encoding utf8
```