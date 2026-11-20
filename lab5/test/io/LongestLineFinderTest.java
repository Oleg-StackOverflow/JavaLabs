package io;

import org.junit.Test;

import io.text.LongestLineFinder;

import java.io.StringReader;
import java.util.Optional;

import static org.junit.Assert.*;

public class LongestLineFinderTest {

    private static Optional<LongestLineFinder.Result> find(String text) throws Exception {
        return LongestLineFinder.find(new StringReader(text));
    }

    @Test
    public void findsLineWithMostWords() throws Exception {
        LongestLineFinder.Result r = find("one two\nthree four five\nsix").orElseThrow();
        assertEquals(2, r.lineNumber());
        assertEquals(3, r.words());
        assertEquals("three four five", r.line());
    }

    @Test
    public void firstWinsOnTie() throws Exception {
        assertEquals(1, find("a b\nc d\n").orElseThrow().lineNumber());
    }

    @Test
    public void longerLineIsNotNecessarilyMoreWords() throws Exception {
        // перший рядок довший за символами, але слів у другому більше
        assertEquals(2, find("Надзвичайно-довге-слово-без-пробілів\nа б в\n").orElseThrow().lineNumber());
    }

    @Test
    public void emptyOrBlankInputHasNoResult() throws Exception {
        assertTrue(find("").isEmpty());
        assertTrue(find("\n   \n\t\n").isEmpty());
    }

    @Test
    public void countWordsHandlesAllKindsOfSpaces() {
        assertEquals(0, LongestLineFinder.countWords(""));
        assertEquals(1, LongestLineFinder.countWords("слово"));
        assertEquals(3, LongestLineFinder.countWords("  a \t b   c  "));
        assertEquals(2, LongestLineFinder.countWords("сто гривень")); // нерозривний пробіл
        assertEquals(1, LongestLineFinder.countWords("кав'ярня"));
        assertEquals(3, LongestLineFinder.countWords("Київ — столиця")); // тире теж "слово" між пробілами
    }

    @Test
    public void windowsLineEndingsWork() throws Exception {
        assertEquals("x y z", find("a\r\nx y z\r\nb").orElseThrow().line());
    }
}
