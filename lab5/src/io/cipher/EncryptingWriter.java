package io.cipher;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

// FilterWriter, який шифрує кожен символ перед передачею далі по ланцюжку
public class EncryptingWriter extends FilterWriter {

    private final ShiftCipher cipher;

    public EncryptingWriter(Writer out, char key) {
        super(out);
        this.cipher = new ShiftCipher(key);
    }

    @Override
    public void write(int c) throws IOException {
        out.write(cipher.encrypt((char) c));
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        // шифруємо в копію, щоб не зіпсувати масив того, хто викликав
        char[] tmp = new char[len];
        for (int i = 0; i < len; i++) {
            tmp[i] = cipher.encrypt(cbuf[off + i]);
        }
        out.write(tmp, 0, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }
}
