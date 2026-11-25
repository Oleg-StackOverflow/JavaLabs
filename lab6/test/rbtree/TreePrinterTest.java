package rbtree;

import org.junit.Test;

import static org.junit.Assert.*;

public class TreePrinterTest {

    private static RedBlackTree<Integer> of(int... values) {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        for (int v : values) t.add(v);
        return t;
    }

    @Test
    public void emptyTree() {
        assertEquals("(порожнє дерево)\n", TreePrinter.topDown(new RedBlackTree<Integer>()));
        assertEquals("(порожнє дерево)\n", TreePrinter.sideways(new RedBlackTree<Integer>()));
    }

    @Test
    public void labelsShowColor() {
        RedBlackTree<Integer> t = of(2, 1, 3);
        assertEquals("[2]", TreePrinter.label(t.root()));
        assertEquals("(1)", TreePrinter.label(t.left(t.root())));
    }

    @Test
    public void topDownOfThreeNodes() {
        String expected =
                "     [2]\n" +
                "  ┌───┴───┐\n" +
                " (1)     (3)\n";
        assertEquals(expected, TreePrinter.topDown(of(2, 1, 3)));
    }

    @Test
    public void topDownShowsSingleChildConnector() {
        String s = TreePrinter.topDown(of(2, 1));
        assertTrue(s, s.contains("┘"));
        String r = TreePrinter.topDown(of(1, 2));
        assertTrue(r, r.contains("└"));
    }

    @Test
    public void sidewaysPutsRightSubtreeOnTop() {
        String expected =
                "    ┌── (3)\n" +
                "─── [2]\n" +
                "    └── (1)\n";
        assertEquals(expected, TreePrinter.sideways(of(2, 1, 3)));
    }

    @Test
    public void everyKeyAppearsExactlyOnce() {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        for (int i = 1; i <= 40; i++) t.add(i * 7 % 41);
        for (String s : new String[]{TreePrinter.topDown(t), TreePrinter.sideways(t)}) {
            for (int k : t.inOrder()) {
                String black = "[" + k + "]", red = "(" + k + ")";
                int count = s.split(java.util.regex.Pattern.quote(black), -1).length - 1
                        + s.split(java.util.regex.Pattern.quote(red), -1).length - 1;
                assertEquals("ключ " + k, 1, count);
            }
        }
    }

    @Test
    public void renderSwitchesToSidewaysForWideTrees() {
        RedBlackTree<Integer> small = of(1, 2, 3);
        RedBlackTree<Integer> big = new RedBlackTree<>();
        for (int i = 1; i <= 50; i++) big.add(i);
        assertEquals(TreePrinter.topDown(small), TreePrinter.render(small, 120));
        assertEquals(TreePrinter.sideways(big), TreePrinter.render(big, 120));
    }
}
