package io.cipher;

import java.io.*;

/*
  Завдання 3: методи шифрування і дешифрування.
  Обидва працюють з будь-якими символьними потоками - файлами, рядками, консоллю.
 */
public final class CipherService {

    private CipherService() {
    }

    // Читає відкритий текст з in і пише шифр в out (через EncryptingWriter).
    public static void encrypt(Reader in, Writer out, char key) throws IOException {
        Writer enc = new EncryptingWriter(out, key);
        in.transferTo(enc);
        enc.flush(); // не закриваємо: потоки належать тому, хто їх відкрив
    }

    // Читає шифр з in (через DecryptingReader) і пише відкритий текст в out.
    public static void decrypt(Reader in, Writer out, char key) throws IOException {
        Reader dec = new DecryptingReader(in, key);
        dec.transferTo(out);
        out.flush();
    }

    public static String encrypt(String text, char key) {
        StringWriter sw = new StringWriter();
        try {
            encrypt(new StringReader(text), sw, key);
        } catch (IOException e) {
            throw new UncheckedIOException(e); // з рядками IOException не буває
        }
        return sw.toString();
    }

    public static String decrypt(String text, char key) {
        StringWriter sw = new StringWriter();
        try {
            decrypt(new StringReader(text), sw, key);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sw.toString();
    }
}
