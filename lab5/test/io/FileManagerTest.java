package io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.files.FileManager;
import io.tags.TagStatistics;
import io.text.LongestLineFinder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;

public class FileManagerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void longestLineInSampleFile() throws Exception {
        LongestLineFinder.Result r = FileManager.findLineWithMostWords(Path.of("samples/text.txt")).orElseThrow();
        assertEquals(5, r.lineNumber());
        assertEquals(14, r.words());
    }

    @Test(expected = FileNotFoundException.class)
    public void missingFile() throws Exception {
        FileManager.findLineWithMostWords(tmp.getRoot().toPath().resolve("nope.txt"));
    }

    @Test
    public void directoryInsteadOfFile() {
        IOException e = assertThrows(IOException.class,
                () -> FileManager.readText(tmp.getRoot().toPath()));
        assertTrue(e.getMessage().startsWith("Це папка"));
    }

    @Test
    public void encryptIntoSameFileRejected() throws Exception {
        Path f = tmp.newFile("a.txt").toPath();
        Files.writeString(f, "текст");
        assertThrows(IOException.class, () -> FileManager.encryptFile(f, f, 'k'));
        assertEquals("текст", Files.readString(f)); // файл не зіпсовано
    }

    @Test
    public void serializationRoundTrip() throws Exception {
        TagStatistics s = new TagStatistics("https://example.com", Map.of("a", 3, "div", 1),
                LocalDateTime.of(2026, 11, 14, 2, 30));
        Path file = tmp.getRoot().toPath().resolve("out/stats.ser");
        FileManager.saveObject(file, s);
        assertTrue(Files.size(file) > 0);

        TagStatistics back = FileManager.loadObject(file, TagStatistics.class);
        assertNotSame(s, back);
        assertEquals(s, back);
        assertEquals(3, back.count("a"));
        assertEquals("a", back.byName().get(0).getKey());
    }

    @Test
    public void loadingTextFileAsObjectGivesClearError() throws Exception {
        Path f = tmp.newFile("not-serialized.txt").toPath();
        Files.writeString(f, "просто текст");
        IOException e = assertThrows(IOException.class, () -> FileManager.loadObject(f, TagStatistics.class));
        assertTrue(e.getMessage().contains("не є збереженими даними"));
    }

    @Test
    public void loadingObjectOfWrongClassRejected() throws Exception {
        Path f = tmp.getRoot().toPath().resolve("str.ser");
        FileManager.saveObject(f, "рядок, а не статистика");
        assertThrows(java.io.InvalidClassException.class, () -> FileManager.loadObject(f, TagStatistics.class));
    }
}
