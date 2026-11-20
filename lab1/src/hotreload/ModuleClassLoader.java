package hotreload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Власний завантажувач класів.
 *
 * Шукає .class-файли у вказаній папці і сам визначає з них класи (defineClass).
 * Головна ідея: один і той самий клас не можна завантажити двічі одним
 * завантажувачем, тому на кожну нову версію TestModule створюється новий
 * екземпляр ModuleClassLoader. Старий разом зі старою версією класу
 * прибирає GC, коли на них більше немає посилань.
 */
public class ModuleClassLoader extends ClassLoader {

    private final Path classesDir;
    private final String reloadableClass;

    public ModuleClassLoader(Path classesDir, String reloadableClass, ClassLoader parent) {
        super(parent);
        this.classesDir = classesDir;
        this.reloadableClass = reloadableClass;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Для нашого класу порушуємо стандартну модель делегування:
        // спочатку шукає сам клас, а не питає батьківський завантажувач.
        // Інакше, якщо клас випадково опиниться в classpath, батько завжди віддаватиме стару версію.
        if (!name.equals(reloadableClass)) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = findClass(name);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Path classFile = classesDir.resolve(name.replace('.', '/') + ".class");
        try {
            byte[] bytes = Files.readAllBytes(classFile);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Не вдалося прочитати " + classFile, e);
        }
    }
}
