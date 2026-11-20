# Task 5 — I/O Streams

Завдання 1, 3, 4 (завдання 2 за умовою пропускається тими, хто виконав Task 3 Mid OOP).

## Структура

- `io.text.LongestLineFinder` — завдання 1: рядок з найбільшою кількістю слів
- `io.cipher` — завдання 3: `EncryptingWriter extends FilterWriter`, `DecryptingReader extends FilterReader`,
  `ShiftCipher` (зсув на код ключа), `CipherService.encrypt/decrypt`
- `io.tags` — завдання 4: `TagCounter` (URL або HTML-файл), `TagStatistics` (Serializable)
- `io.files.FileManager` — уся робота з файлами (вимога 4), включно з ObjectOutputStream/ObjectInputStream
- `io.app` — консольне меню (`Main`) і введення з перевірками (`ConsoleInput`)
- `samples/` — тестові файли: `text.txt`, `page.html`
- `test/io` — JUnit 4 тести, `lib/` — junit + hamcrest

## Запуск (Windows, PowerShell)

Потрібен JDK 17+. Усі команди виконувати з кореня проєкту (`lab5`): програма і тести шукають файли в `samples/` відносно поточної папки.

Компіляція:

```powershell
javac -encoding UTF-8 -d out/main (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Меню:

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main io.app.Main
```

Що вводити в меню:
- пункт 1: `samples/text.txt`
- пункт 2: `samples/text.txt`, потім куди записати шифр, наприклад `out-files/secret.txt`, потім ключ — один символ
- пункт 3: `out-files/secret.txt`, `out-files/decrypted.txt` і той самий ключ
- пункт 5: URL, наприклад `https://uk.wikipedia.org/`, або `samples/page.html`
- пункт 6: файл для збереження статистики, наприклад `out-files/stats.ser`; пункт 7 — завантажити його назад

Перевірити, що розшифрований файл збігається з оригіналом:

```powershell
fc.exe /b samples\text.txt out-files\decrypted.txt
```

## Тести

Очікуваний результат: `OK (32 tests)`.

```powershell
$cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
java "-Dfile.encoding=UTF-8" -cp $cp org.junit.runner.JUnitCore io.LongestLineFinderTest io.CipherTest io.TagCounterTest io.FileManagerTest
```
