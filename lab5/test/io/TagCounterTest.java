package io;

import org.junit.BeforeClass;
import org.junit.Test;

import io.files.FileManager;
import io.tags.TagCounter;
import io.tags.TagStatistics;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TagCounterTest {

    private static TagStatistics page;

    @BeforeClass
    public static void loadSample() throws Exception {
        page = TagCounter.count(FileManager.readText(Path.of("samples/page.html")), "samples/page.html");
    }

    @Test
    public void countsOpeningAndSelfClosingTags() {
        assertEquals(3, page.count("a"));
        assertEquals(3, page.count("li"));
        assertEquals(2, page.count("br"));     // <br/> і <br>
        assertEquals(1, page.count("img"));    // <img .../>
        assertEquals(1, page.count("meta"));   // без закриваючого
    }

    @Test
    public void caseInsensitive() {
        assertEquals(1, page.count("html"));   // <HTML>
        assertEquals(2, page.count("p"));      // <P> і <p>
    }

    @Test
    public void ignoresClosingTagsDoctypeAndComments() {
        assertEquals(1, page.count("div"));    // закоментований і в JS не рахуються
        assertEquals(0, page.count("!doctype"));
        assertEquals(24, page.totalTags());
        assertEquals(17, page.distinctTags());
    }

    @Test
    public void scriptAndStyleContentIsNotParsed() {
        assertEquals(2, page.count("script")); // сам тег рахується
        assertEquals(1, page.count("style"));
        TagStatistics s = TagCounter.count("<script>if (a<b) x('<span>')</script><SCRIPT>'<i>'</SCRIPT>", "t");
        assertEquals(2, s.count("script"));
        assertEquals(0, s.count("b"));
        assertEquals(0, s.count("span"));
        assertEquals(0, s.count("i"));
    }

    @Test
    public void notATagWhenNoLetterAfterBracket() {
        TagStatistics s = TagCounter.count("<p>1 < 2 and 3 <4 and <= </p>", "t");
        assertEquals(1, s.totalTags());
    }

    @Test
    public void sortedByNameLexicographically() {
        List<Map.Entry<String, Integer>> rows = page.byName();
        for (int i = 1; i < rows.size(); i++) {
            assertTrue(rows.get(i - 1).getKey().compareTo(rows.get(i).getKey()) < 0);
        }
        assertEquals("a", rows.get(0).getKey());
        assertEquals("ul", rows.get(rows.size() - 1).getKey());
    }

    @Test
    public void sortedByFrequencyAscendingThenName() {
        List<Map.Entry<String, Integer>> rows = page.byFrequency();
        for (int i = 1; i < rows.size(); i++) {
            Map.Entry<String, Integer> prev = rows.get(i - 1), cur = rows.get(i);
            assertTrue(prev.getValue() < cur.getValue()
                    || (prev.getValue().equals(cur.getValue()) && prev.getKey().compareTo(cur.getKey()) < 0));
        }
        assertEquals(1, (int) rows.get(0).getValue());
        assertEquals(3, (int) rows.get(rows.size() - 1).getValue());
        assertEquals("li", rows.get(rows.size() - 1).getKey()); // a=3, li=3 -> li останній
    }

    @Test(expected = IllegalArgumentException.class)
    public void downloadRejectsNonHttp() throws Exception {
        TagCounter.download("ftp://example.com");
    }
}
