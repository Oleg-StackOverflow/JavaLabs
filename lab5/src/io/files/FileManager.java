package io.files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import io.cipher.CipherService;
import io.text.LongestLineFinder;

/*
 Уся робота з файлами зібрана тут (вимога 4). Решта класів працює з Reader/Writer
 або рядками і про файли нічого не знає - так їх легко тестувати без диска.
 Усі текстові файли - UTF-8. Потоки закриваються через try-with-resources.
 */
public final class FileManager {

    private FileManager() {
    }

    // ---------------- завдання 1 ----------------

    public static Optional<LongestLineFinder.Result> findLineWithMostWords(Path file) throws IOException {
        checkReadable(file);
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return LongestLineFinder.find(r);
        }
    }

    // ---------------- завдання 3 ----------------

    public static void encryptFile(Path source, Path target, char key) throws IOException {
        checkReadable(source);
        checkNotSame(source, target);
        prepareParent(target);
        try (Reader in = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             Writer out = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            CipherService.encrypt(in, out, key);
        }
    }

    public static void decryptFile(Path source, Path target, char key) throws IOException {
        checkReadable(source);
        checkNotSame(source, target);
        prepareParent(target);
        try (Reader in = Files.newBufferedReader(source, StandardCharsets.UTF_8);
             Writer out = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            CipherService.decrypt(in, out, key);
        }
    }

    // ---------------- завдання 4 ----------------

    public static String readText(Path file) throws IOException {
        checkReadable(file);
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public static void writeText(Path file, String text) throws IOException {
        prepareParent(file);
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    // Об'єктний потік + стандартна серіалізація (вимоги 2, 3)
    public static void saveObject(Path file, Serializable obj) throws IOException {
        prepareParent(file);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeObject(obj);
        }
    }

    // Читає об'єкт і перевіряє, що він потрібного класу
    public static <T> T loadObject(Path file, Class<T> type) throws IOException, ClassNotFoundException {
        checkReadable(file);
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            Object o = in.readObject();
            if (!type.isInstance(o)) {
                throw new InvalidClassException("У файлі " + o.getClass().getName() + ", а очікувався " + type.getName());
            }
            return type.cast(o);
        } catch (StreamCorruptedException | EOFException e) {
            throw new IOException("Файл " + file + " не є збереженими даними програми (або пошкоджений)", e);
        }
    }

    // ---------------- перевірки ----------------

    private static void checkReadable(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new FileNotFoundException("Файл не знайдено: " + file.toAbsolutePath());
        }
        if (Files.isDirectory(file)) {
            throw new IOException("Це папка, а не файл: " + file.toAbsolutePath());
        }
        if (!Files.isReadable(file)) {
            throw new IOException("Немає прав на читання: " + file.toAbsolutePath());
        }
    }

    // шифрувати файл "сам у себе" не можна: відкриття на запис обнулить його до читання
    private static void checkNotSame(Path a, Path b) throws IOException {
        if (Files.exists(b) && Files.isSameFile(a, b)) {
            throw new IOException("Вхідний і вихідний файл не можуть збігатися");
        }
    }

    private static void prepareParent(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.isDirectory(file)) {
            throw new IOException("Це папка, а не файл: " + file.toAbsolutePath());
        }
    }
}
