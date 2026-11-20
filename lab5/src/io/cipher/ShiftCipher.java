package io.cipher;

/*
 Зсув символу на код ключового символу.
 Простий варіант (char) (c + key) має пастку: результат може потрапити в діапазон
 сурогатів UTF-16 (U+D800..U+DFFF). Такий "самотній" сурогат не можна записати у файл
 в UTF-8 - кодувальник замінить його на '?', і розшифрувати текст уже не вийде.
 Тому зсув робиться по колу з 63488 "звичайних" символів BMP, у якому сурогатів немає.
 Для звичайного тексту (латиниця, кирилиця) це той самий c + key.
 Сурогатні пари (емодзі тощо) пропускаються без змін, щоб не зламати їх.
 */
public final class ShiftCipher {

    private static final int SURR_START = 0xD800;
    private static final int SURR_COUNT = 0xE000 - 0xD800;      // 2048
    private static final int SIZE = 0x10000 - SURR_COUNT;       // 63488

    private final int shift;

    public ShiftCipher(char key) {
        if (Character.isSurrogate(key)) {
            throw new IllegalArgumentException("Ключ не може бути сурогатним символом");
        }
        if (key == 0) {
            throw new IllegalArgumentException("Ключ з кодом 0 нічого не шифрує");
        }
        this.shift = toIndex(key) % SIZE;
    }

    public char encrypt(char c) {
        if (Character.isSurrogate(c)) return c;
        return fromIndex((toIndex(c) + shift) % SIZE);
    }

    public char decrypt(char c) {
        if (Character.isSurrogate(c)) return c;
        return fromIndex((toIndex(c) - shift + SIZE) % SIZE);
    }

    private static int toIndex(char c) {
        return c < SURR_START ? c : c - SURR_COUNT;
    }

    private static char fromIndex(int i) {
        return (char) (i < SURR_START ? i : i + SURR_COUNT);
    }
}
