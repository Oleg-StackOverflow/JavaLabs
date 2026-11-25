# Hard Task 6 — червоно-чорне дерево

## Структура

- `src/rbtree/RedBlackTree.java` — червоно-чорне дерево (алгоритми CLRS, гл. 13): `add`, `remove`, `contains`,
  обходи `inOrder` / `preOrder` / `postOrder` / `levelOrder`, `height`, `blackHeight`,
  `checkInvariants` (перевірка всіх 5 властивостей — для тестів)
- `src/rbtree/TreePrinter.java` — відображення дерева: зверху вниз або боком (для широких дерев)
- `src/rbtree/app/Main.java` — консольне меню і демо-сценарій
- `test/rbtree` — JUnit 4 тести (24 шт.), `lib/` — junit + hamcrest

Позначення на рисунках: `[x]` — чорний вузол, `(x)` — червоний вузол.

## Запуск (Windows, PowerShell)

Потрібен JDK 17+. Усі команди виконувати з кореня проєкту.


Компіляція:

```powershell
javac -encoding UTF-8 -d out/main (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Меню:

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main rbtree.app.Main
```

Пункти меню:
- 1 — випадкові числа в довільному порядку (масив виводиться, щоб було видно порядок додавання)
- 2 — випадкові числа, впорядковані за зростанням
- 3 — додати числа з клавіатури (через пробіл або кому: `10 20, 30`)
- 4 — видалити числа, 5 — пошук, 6 — показати дерево, 7 — обходи, 8 — очистити

Готовий демо-сценарій (випадкові і впорядковані числа, введення, видалення, велике дерево):

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main rbtree.app.Main --demo
```

Дерево до 120 символів завширшки друкується зверху вниз, ширше — боком (корінь зліва, праве піддерево зверху).
Щоб рисунок не "розлазився", розгорніть вікно терміналу на всю ширину.

## Тести

```powershell
.\run-tests.ps1
```
Або вручну:

```powershell
$cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
java -cp $cp org.junit.runner.JUnitCore rbtree.RedBlackTreeTest rbtree.TreePrinterTest
```


## Примітка

- Демо використовує фіксований seed (2026), тому при кожному запуску дає ті самі "випадкові" числа; у меню числа щоразу нові.
