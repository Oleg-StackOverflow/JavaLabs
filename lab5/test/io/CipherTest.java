package io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.cipher.*;
import io.files.FileManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class CipherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void shiftsByKeyCodeForOrdinaryText() {
        // 'A' (65) + 'K' (75) = 140
        assertEquals("" + (char) 140, CipherService.encrypt("A", 'K'));
        // кирилиця: 'К' (U+041A) + '\u0003' = U+041D 'Н'
        assertEquals("Н", CipherService.encrypt("К", '\u0003'));
    }

    @Test
    public void decryptRestoresText() {
        String text = "Привіт, світе! Hello 123\n\tкав'ярня — «лапки»";
        for (char key : new char[]{'K', 'я', '\u0001', '￿', 'ꯍ'}) {
            String enc = CipherService.encrypt(text, key);
            assertNotEquals(text, enc);
            assertEquals(text, CipherService.decrypt(enc, key));
        }
    }

    @Test
    public void wrongKeyGivesWrongText() {
        String enc = CipherService.encrypt("секрет", 'a');
        assertNotEquals("секрет", CipherService.decrypt(enc, 'b'));
    }

    @Test
    public void everyNonSurrogateCharRoundTrips() {
        ShiftCipher c = new ShiftCipher('睷');
        for (int i = 0; i <= 0xFFFF; i++) {
            char ch = (char) i;
            char e = c.encrypt(ch);
            assertEquals(Character.isSurrogate(ch), Character.isSurrogate(e)); // шифр ніколи не дає сурогат
            assertEquals(ch, c.decrypt(e));
        }
    }

    @Test
    public void emojiSurvive() {
        String text = "лижі 🎿 і сніг ❄";
        assertEquals(text, CipherService.decrypt(CipherService.encrypt(text, 'Ж'), 'Ж'));
    }

    @Test
    public void filterStreamsWorkCharByCharAndInBlocks() throws Exception {
        StringWriter sw = new StringWriter();
        try (Writer w = new EncryptingWriter(sw, 'q')) {
            w.write('a');                     // write(int)
            w.write("bcd", 1, 2);             // write(String, off, len) -> "cd"
            w.write(new char[]{'x', 'e', 'y'}, 1, 1); // write(char[], off, len) -> "e"
        }
        try (Reader r = new DecryptingReader(new StringReader(sw.toString()), 'q')) {
            assertEquals('a', r.read());      // read()
            char[] buf = new char[10];
            int n = r.read(buf, 2, 10 - 2);   // read(char[], off, len)
            assertEquals("cde", new String(buf, 2, n));
            assertEquals(-1, r.read());
        }
    }

    @Test
    public void callerArrayIsNotModified() throws Exception {
        char[] data = {'a', 'b'};
        new EncryptingWriter(new StringWriter(), 'k').write(data, 0, 2);
        assertArrayEquals(new char[]{'a', 'b'}, data);
    }

    @Test
    public void fileRoundTripInUtf8() throws Exception {
        Path plain = tmp.newFile("plain.txt").toPath();
        Path enc = tmp.getRoot().toPath().resolve("sub/enc.txt"); // папки ще немає
        Path dec = tmp.getRoot().toPath().resolve("dec.txt");
        String text = "Київ\nДніпро 🎿\n";
        Files.writeString(plain, text, StandardCharsets.UTF_8);

        FileManager.encryptFile(plain, enc, '가');
        FileManager.decryptFile(enc, dec, '가');
        assertNotEquals(text, Files.readString(enc, StandardCharsets.UTF_8));
        assertEquals(text, Files.readString(dec, StandardCharsets.UTF_8));
    }

    /*
      Чому зсув "по колу без сурогатів": ідеографічний пробіл U+3000 + ключ U+AC00 = U+DC00.
      Наївний (char)(c + key) дає самотній сурогат, і UTF-8 його втрачає ('?').
     */
    @Test
    public void naiveShiftWouldBreakButOursDoesNot() throws Exception {
        char key = '가';
        String text = "a　b";
        char naive = (char) ('　' + key);
        assertTrue(Character.isSurrogate(naive));
        String naiveThroughUtf8 = new String(String.valueOf(naive).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        assertEquals("?", naiveThroughUtf8);                            // дані втрачено

        Path enc = tmp.getRoot().toPath().resolve("enc.txt");
        Path plain = tmp.getRoot().toPath().resolve("p.txt");
        Path dec = tmp.getRoot().toPath().resolve("d.txt");
        Files.writeString(plain, text, StandardCharsets.UTF_8);
        FileManager.encryptFile(plain, enc, key);
        FileManager.decryptFile(enc, dec, key);
        assertEquals(text, Files.readString(dec, StandardCharsets.UTF_8)); // наш варіант - без втрат
    }

    @Test(expected = IllegalArgumentException.class)
    public void surrogateKeyRejected() {
        new ShiftCipher('\uD800');
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroKeyRejected() {
        new ShiftCipher('\0');
    }
}
