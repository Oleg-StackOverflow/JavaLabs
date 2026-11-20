package io.app;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
  Введення з клавіатури з перевірками (вимоги 1, 5, 6).
  Неправильне значення не "валить" програму - користувач просто вводить ще раз.
 */
public class ConsoleInput {

    // Кінець вводу (Ctrl+D / Ctrl+Z) - щоб меню могло коректно завершитись. */
    public static class InputClosedException extends RuntimeException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public InputClosedException() {
            super("Ввід закрито");
        }
    }

    private final Scanner in;
    private final PrintStream out;

    public ConsoleInput(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public String line(String prompt) {
        out.print(prompt);
        try {
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new InputClosedException();
        }
    }

    public String nonEmpty(String prompt) {
        while (true) {
            String s = line(prompt);
            if (!s.isEmpty()) return s;
            out.println("  Значення не може бути порожнім.");
        }
    }

    // Шлях до файлу. Перевіряється лише синтаксис - існування перевіряє FileManager
    public Path path(String prompt) {
        while (true) {
            String s = nonEmpty(prompt);
            // прибираємо лапки, які додає Windows при "Копіювати як шлях"
            if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
                s = s.substring(1, s.length() - 1);
            }
            try {
                return Path.of(s);
            } catch (InvalidPathException e) {
                out.println("  Некоректний шлях: " + e.getReason());
            }
        }
    }

    // Рівно один символ - ключ шифру
    public char keyChar(String prompt) {
        while (true) {
            String s = line(prompt);
            try {
                if (s.length() != 1) {
                    throw new IllegalArgumentException("потрібен рівно один символ, а введено " + s.length());
                }
                char c = s.charAt(0);
                if (Character.isSurrogate(c)) {
                    throw new IllegalArgumentException("символ поза BMP (емодзі тощо) не підходить");
                }
                return c;
            } catch (IllegalArgumentException e) {
                out.println("  Неправильний ключ: " + e.getMessage());
            }
        }
    }

    public int intInRange(String prompt, int min, int max) {
        while (true) {
            String s = line(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    throw new IllegalArgumentException("число має бути від " + min + " до " + max);
                }
                return v;
            } catch (NumberFormatException e) {
                out.println("  Це не ціле число: \"" + s + "\"");
            } catch (IllegalArgumentException e) {
                out.println("  " + e.getMessage());
            }
        }
    }
}
