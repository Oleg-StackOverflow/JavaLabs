# Hard Task власний ClassLoader який «на льоту» підвантажує зміни

Структура:
- `modules/TestModule.java` клас, за яким стежить програма (не в classpath)
- `src/hotreload/ModuleClassLoader.java`  власний завантажувач класів
- `src/hotreload/ModuleCompiler.java`  компіляція через javax.tools
- `src/hotreload/Main.java`  WatchService + перезавантаження

Запуск (потрібен JDK 17+):
    javac -d out src/hotreload/*.java
    java -cp out hotreload.Main modules

Далі відкрийте modules/TestModule.java, змініть рядок у toString() і збережіть
у консолі з'явиться нове повідомлення. Зупинка  Ctrl+C.
