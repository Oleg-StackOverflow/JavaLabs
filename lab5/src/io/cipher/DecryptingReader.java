package io.cipher;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

// FilterReader, який розшифровує символи, прочитані з нижнього потоку
public class DecryptingReader extends FilterReader {

    private final ShiftCipher cipher;

    public DecryptingReader(Reader in, char key) {
        super(in);
        this.cipher = new ShiftCipher(key);
    }

    @Override
    public int read() throws IOException {
        int c = in.read();
        return c == -1 ? -1 : cipher.decrypt((char) c);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = in.read(cbuf, off, len);
        for (int i = 0; i < n; i++) {   // n == -1 -> цикл не виконається
            cbuf[off + i] = cipher.decrypt(cbuf[off + i]);
        }
        return n;
    }
}
