package hotreload;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Компіляція java-файлу "на льоту" через javax.tools.JavaCompiler
 * той самий javac, тільки викликаний з коду. Працює лише під JDK
 */
public class ModuleCompiler {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public ModuleCompiler() {
        if (compiler == null) {
            throw new IllegalStateException("Компілятор недоступний - запустіть програму з JDK, а не JRE");
        }
    }

    /** @return true, якщо компіляція пройшла без помилок */
    public boolean compile(Path sourceFile, Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int result = compiler.run(null, null, errors,
                "-d", outputDir.toString(),
                sourceFile.toString());
        if (result != 0) {
            System.out.println("[compiler] Помилка компіляції:");
            System.out.println(errors.toString().trim());
            return false;
        }
        return true;
    }
}
