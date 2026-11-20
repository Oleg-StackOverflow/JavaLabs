package io.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/*
 Завдання 1: рядок з максимальною кількістю слів.
 Файл читається построково через BufferedReader, тож працює і для великих файлів
 (у пам'яті тримаємо лише поточний рядок і найкращий результат).
 */
public final class LongestLineFinder {

    // Результат: номер рядка (з 1), сам рядок і кількість слів у ньому
    public record Result(int lineNumber, String line, int words) {
    }

    private LongestLineFinder() {
    }

    /* 
     @return рядок з найбільшою кількістю слів; якщо таких кілька - перший з них.
     Порожньо, якщо у файлі немає жодного слова.
     */
    public static Optional<Result> find(Reader source) throws IOException {
        BufferedReader in = (source instanceof BufferedReader b) ? b : new BufferedReader(source);
        Result best = null;
        String line;
        int number = 0;
        while ((line = in.readLine()) != null) {
            number++;
            int words = countWords(line);
            if (words > 0 && (best == null || words > best.words())) {
                best = new Result(number, line, words);
            }
        }
        return Optional.ofNullable(best);
    }

    /*
     Слово - послідовність символів між пробільними символами.
     Нерозривний пробіл ( ) теж вважаємо роздільником: Character.isWhitespace
     його не бачить, тому додатково перевіряємо isSpaceChar.
     */
    public static int countWords(String line) {
        int count = 0;
        boolean inWord = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            boolean space = Character.isWhitespace(c) || Character.isSpaceChar(c);
            if (!space && !inWord) {
                count++;
            }
            inWord = !space;
        }
        return count;
    }
}
