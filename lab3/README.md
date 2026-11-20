# Middle Task 3.3 — турнікет лижного підйомника (ski-pass)

## Структура

- `skipass.model` — види ski-pass (`SkiPassType`), дані картки (`SkiPass` + 3 підкласи), фізична картка `Card`, причини відмов (`DenyReason`)
- `skipass.system` — `SkiPassSystem`: реєстр карток, випуск і блокування
- `skipass.turnstile` — `Turnstile`: перевірка проходу, `TurnstileStats`: облік дозволів і відмов
- `skipass.app` — консольне меню і демо-сценарій (`Main`), керований годинник (`SimulatedClock`)
- `test/skipass` — JUnit 4 тести, `lib/` — junit + hamcrest

## Запуск (Windows, PowerShell)

Потрібен JDK 17+. Усі команди виконувати з кореня проєкту (`lab3`).


Компіляція:

```powershell
javac -encoding UTF-8 -d out/main (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Інтерактивне меню:

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main skipass.app.Main
```

Готовий демо-сценарій (тиждень сезону, усі види відмов, обидва звіти):

```powershell
java "-Dstdout.encoding=UTF-8" -cp out/main skipass.app.Main --demo
```

## Тести

Очікуваний результат: `OK (38 tests)`.

```powershell
$cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
java -cp $cp org.junit.runner.JUnitCore skipass.SkiPassModelTest skipass.SkiPassSystemTest skipass.TurnstileTest
```

## Припущення, яких немає в умові

- сезон 01.12.2026–31.03.2027, підйомник працює 9:00–17:00;
- картки "по підйомах" діють до кінця сезону, але лише у свою категорію днів;
- "2 дні"/"5 днів" — підряд ідучі дні своєї категорії (5 буднів з четверга = чт, пт, пн, вт, ср);
- картка на будні, куплена у вихідний (і навпаки), активується з найближчого підходящого дня.
