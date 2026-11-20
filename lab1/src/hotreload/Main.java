package hotreload;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * Програма створює екземпляр TestModule, друкує його і висить у пам'яті,
 * стежачи за файлом TestModule.java. Після кожного збереження файлу клас
 * перекомпілюється, перезавантажується новим ModuleClassLoader і друкується знову.
 *
 * Запуск:  java -cp out hotreload.Main
 */
public class Main {

    private static final String CLASS_NAME = "TestModule";

    private final Path sourceDir;
    private final Path sourceFile;
    private final Path classesDir;
    private final ModuleCompiler compiler = new ModuleCompiler();

    // Тримаємо посилання як Object: класу TestModule немає в classpath програми,
    // тому привести до нього тип неможливо
    private Object current;
    private int version = 0;

    public Main(Path sourceDir) {
        this.sourceDir = sourceDir.toAbsolutePath();
        this.sourceFile = this.sourceDir.resolve(CLASS_NAME + ".java");
        this.classesDir = Paths.get("build", "modules").toAbsolutePath();
    }

    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(args.length > 0 ? args[0] : "modules");
        new Main(dir).run();
    }

    private void run() throws Exception {
        System.out.println("Стежу за файлом: " + sourceFile);
        reload();

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            sourceDir.register(watcher,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey key = watcher.take(); // блокуємо, поки щось не зміниться
                boolean changed = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    Object ctx = event.context();
                    if (ctx instanceof Path p && p.toString().equals(CLASS_NAME + ".java")) {
                        changed = true;
                    }
                }
                key.reset();

                if (changed) {
                    TimeUnit.MILLISECONDS.sleep(300);
                    WatchKey extra;
                    while ((extra = watcher.poll()) != null) {
                        extra.pollEvents();
                        extra.reset();
                    }
                    reload();
                }
            }
        }
    }

    private void reload() {
        try {
            if (!compiler.compile(sourceFile, classesDir)) {
                System.out.println("[reload] Залишаю попередню версію.");
                return;
            }
            ClassLoader loader = new ModuleClassLoader(classesDir, CLASS_NAME, Main.class.getClassLoader());
            Class<?> cls = loader.loadClass(CLASS_NAME);
            current = cls.getDeclaredConstructor().newInstance();
            version++;

            System.out.println("---- перезавантаження #" + version
                    + " | loader@" + Integer.toHexString(System.identityHashCode(loader))
                    + " | class@" + Integer.toHexString(System.identityHashCode(cls)) + " ----");
            System.out.println(current);
        } catch (Exception e) {
            System.out.println("[reload] Не вдалося завантажити клас: " + e);
        }
    }
}
