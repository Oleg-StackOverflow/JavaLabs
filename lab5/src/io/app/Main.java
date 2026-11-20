package io.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

import io.cipher.CipherService;
import io.files.FileManager;
import io.tags.TagCounter;
import io.tags.TagStatistics;
import io.text.LongestLineFinder;

// Task 5 (I/O Streams): консольне меню для завдань 1, 3, 4
public class Main {

    private final ConsoleInput input;
    private TagStatistics lastStats; // останній результат завдання 4 - для збереження у файл

    Main(ConsoleInput input) {
        this.input = input;
    }

    public static void main(String[] args) {
        new Main(new ConsoleInput(new Scanner(System.in), System.out)).run();
    }

    void run() {
        System.out.println("Task 5 - I/O Streams");
        try {
            while (true) {
                System.out.println();
                System.out.println("1 - рядок з найбільшою кількістю слів у файлі");
                System.out.println("2 - зашифрувати файл         3 - розшифрувати файл");
                System.out.println("4 - зашифрувати/розшифрувати введений рядок");
                System.out.println("5 - частота тегів (URL або HTML-файл)");
                System.out.println("6 - зберегти статистику тегів у файл");
                System.out.println("7 - завантажити статистику тегів з файлу");
                System.out.println("0 - вихід");
                int choice = input.intInRange("> ", 0, 7);
                if (choice == 0) {
                    return;
                }
                // одна обробка помилок для всіх пунктів: помилка -> повідомлення -> знову меню
                try {
                    switch (choice) {
                        case 1 -> longestLine();
                        case 2 -> encryptFile();
                        case 3 -> decryptFile();
                        case 4 -> encryptString();
                        case 5 -> countTags();
                        case 6 -> saveStats();
                        case 7 -> loadStats();
                        default -> throw new IllegalStateException();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Помилка: " + e.getMessage());
                } catch (HttpTimeoutException e) {
                    System.out.println("Сервер не відповів вчасно: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Помилка вводу-виводу: " + describe(e));
                } catch (ClassNotFoundException e) {
                    System.out.println("У файлі об'єкт невідомого класу: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Некоректні дані: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Операцію перервано.");
                    return;
                }
            }
        } catch (ConsoleInput.InputClosedException e) {
            System.out.println();
        } finally {
            System.out.println("До побачення.");
        }
    }

    private void longestLine() throws IOException {
        Path file = input.path("Файл: ");
        Optional<LongestLineFinder.Result> r = FileManager.findLineWithMostWords(file);
        if (r.isEmpty()) {
            System.out.println("У файлі немає жодного слова.");
        } else {
            System.out.printf("Рядок %d, слів: %d%n  %s%n", r.get().lineNumber(), r.get().words(), r.get().line());
        }
    }

    private void encryptFile() throws IOException {
        Path src = input.path("Файл з текстом: ");
        Path dst = input.path("Куди записати шифр: ");
        char key = input.keyChar("Ключовий символ: ");
        FileManager.encryptFile(src, dst, key);
        System.out.println("Готово: " + dst.toAbsolutePath());
    }

    private void decryptFile() throws IOException {
        Path src = input.path("Зашифрований файл: ");
        Path dst = input.path("Куди записати текст: ");
        char key = input.keyChar("Ключовий символ: ");
        FileManager.decryptFile(src, dst, key);
        System.out.println("Готово: " + dst.toAbsolutePath());
    }

    private void encryptString() {
        String text = input.nonEmpty("Текст: ");
        char key = input.keyChar("Ключовий символ: ");
        String enc = CipherService.encrypt(text, key);
        System.out.println("Шифр:        " + enc);
        System.out.println("Розшифровка: " + CipherService.decrypt(enc, key));
    }

    private void countTags() throws IOException, InterruptedException {
        String where = input.nonEmpty("URL (http/https) або шлях до HTML-файлу: ");
        String html = where.matches("(?i)https?://.*")
                ? TagCounter.download(where)
                : FileManager.readText(Path.of(where));
        lastStats = TagCounter.count(html, where);
        System.out.println(lastStats);
        printStats(lastStats);
    }

    private void saveStats() throws IOException {
        if (lastStats == null) {
            System.out.println("Спершу порахуйте теги (пункт 5).");
            return;
        }
        Path file = input.path("Файл для збереження (наприклад stats.ser): ");
        FileManager.saveObject(file, lastStats);
        System.out.println("Збережено: " + file.toAbsolutePath());
    }

    private void loadStats() throws IOException, ClassNotFoundException {
        Path file = input.path("Файл зі статистикою: ");
        lastStats = FileManager.loadObject(file, TagStatistics.class);
        System.out.println("Завантажено: " + lastStats);
        printStats(lastStats);
    }

    // У деяких винятків (наприклад ConnectException) немає тексту - тоді показуємо хоча б тип
    private static String describe(Exception e) {
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() + " (немає з'єднання?)" : msg;
    }

    private static void printStats(TagStatistics s) {
        System.out.println("а) теги в лексикографічному порядку:");
        System.out.print(TagStatistics.format(s.byName()));
        System.out.println("б) за зростанням частоти:");
        System.out.print(TagStatistics.format(s.byFrequency()));
    }
}
