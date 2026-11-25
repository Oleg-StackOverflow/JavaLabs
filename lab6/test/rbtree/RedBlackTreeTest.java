package rbtree;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RedBlackTreeTest {

    private RedBlackTree<Integer> tree;

    @Before
    public void setUp() {
        tree = new RedBlackTree<>();
    }

    private void addAll(int... values) {
        for (int v : values) {
            tree.add(v);
            tree.checkInvariants();   // властивості мають триматися після КОЖНОЇ операції
        }
    }

    // базове

    @Test
    public void emptyTree() {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.height());
        assertEquals(0, tree.blackHeight());
        assertNull(tree.root());
        assertEquals(List.of(), tree.inOrder());
        assertEquals(List.of(), tree.levelOrder());
        assertFalse(tree.remove(1));
        tree.checkInvariants();
    }

    @Test
    public void singleNodeIsBlackRoot() {
        assertTrue(tree.add(5));
        assertEquals(RedBlackTree.Color.BLACK, tree.root().color());
        assertEquals(1, tree.height());
    }

    @Test
    public void duplicatesAreIgnored() {
        assertTrue(tree.add(5));
        assertFalse(tree.add(5));
        assertEquals(1, tree.size());
    }

    @Test
    public void containsAndRemoveMissing() {
        addAll(10, 20, 30);
        assertTrue(tree.contains(20));
        assertFalse(tree.contains(25));
        assertFalse(tree.remove(25));
        assertEquals(3, tree.size());
    }

    @Test(expected = NullPointerException.class)
    public void nullRejected() {
        tree.add(null);
    }

    // випадки балансування при вставці

    @Test
    public void insertCase3RotatesRight() {
        addAll(30, 20, 10);              // лівий "ланцюжок" -> поворот вправо
        assertEquals(20, (int) tree.root().key());
        assertEquals(List.of(List.of(20), List.of(10, 30)), tree.levelOrder());
        assertTrue(tree.left(tree.root()).isRed());
        assertTrue(tree.right(tree.root()).isRed());
    }

    @Test
    public void insertCase2DoubleRotation() {
        addAll(30, 10, 20);              // "зигзаг" -> два повороти
        assertEquals(20, (int) tree.root().key());
    }

    @Test
    public void insertCase1Recolors() {
        addAll(20, 10, 30, 5);           // дядько (30) червоний -> лише перефарбування
        assertEquals(List.of(List.of(20), List.of(10, 30), List.of(5)), tree.levelOrder());
        assertFalse(tree.left(tree.root()).isRed());
        assertFalse(tree.right(tree.root()).isRed());
        assertTrue(tree.left(tree.left(tree.root())).isRed());
    }

    @Test
    public void sortedOneToTenMatchesTextbookShape() {
        for (int i = 1; i <= 10; i++) addAll(i);
        assertEquals(4, (int) tree.root().key());
        assertEquals(List.of(4, 2, 1, 3, 6, 5, 8, 7, 9, 10), tree.preOrder());
        assertEquals(5, tree.height());       // 4 -> 6 -> 8 -> 9 -> 10
        assertEquals(3, tree.blackHeight());  // [4] -> [2] -> [1]
    }

    //  обходи 

    @Test
    public void traversals() {
        addAll(50, 30, 70, 20, 40, 60, 80);   // ідеально збалансоване, без поворотів
        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), tree.inOrder());
        assertEquals(List.of(50, 30, 20, 40, 70, 60, 80), tree.preOrder());
        assertEquals(List.of(20, 40, 30, 60, 80, 70, 50), tree.postOrder());
        assertEquals(List.of(List.of(50), List.of(30, 70), List.of(20, 40, 60, 80)), tree.levelOrder());
    }

    //  видалення 

    @Test
    public void removeLeafNodeWithOneChildAndWithTwoChildren() {
        addAll(50, 30, 70, 20, 40, 60, 80, 10);
        assertTrue(tree.remove(10));     // лист
        tree.checkInvariants();
        assertTrue(tree.remove(20));     // ...
        tree.checkInvariants();
        assertTrue(tree.remove(30));     // вузол з однією дитиною
        tree.checkInvariants();
        assertTrue(tree.remove(50));     // корінь з двома дітьми -> наступник 60
        tree.checkInvariants();
        assertEquals(List.of(40, 60, 70, 80), tree.inOrder());
    }

    @Test
    public void removeEverythingInVariousOrders() {
        for (int order = 0; order < 3; order++) {
            tree.clear();
            List<Integer> keys = new ArrayList<>();
            for (int i = 1; i <= 200; i++) keys.add(i);
            keys.forEach(tree::add);
            if (order == 1) Collections.reverse(keys);
            if (order == 2) Collections.shuffle(keys, new Random(order));
            for (int k : keys) {
                assertTrue(tree.remove(k));
                tree.checkInvariants();
            }
            assertTrue(tree.isEmpty());
            assertNull(tree.root());
        }
    }

    //  висота 

    @Test
    public void heightStaysLogarithmicForSortedInput() {
        for (int n : new int[]{10, 100, 1000, 100_000}) {
            tree.clear();
            for (int i = 0; i < n; i++) tree.add(i);   // найгірший вхід для звичайного BST
            tree.checkInvariants();
            double bound = 2 * Math.log(n + 1) / Math.log(2);
            assertTrue("n=" + n + ", h=" + tree.height(), tree.height() <= bound);
        }
    }

    @Test
    public void heightStaysLogarithmicForDescendingInput() {
        for (int i = 10_000; i > 0; i--) tree.add(i);
        tree.checkInvariants();
        assertTrue(tree.height() <= 2 * Math.log(10_001) / Math.log(2));
    }

    //  порівняння з java.util.TreeSet 

    @Test
    public void randomOperationsMatchTreeSet() {
        for (long seed = 0; seed < 50; seed++) {
            Random rnd = new Random(seed);
            RedBlackTree<Integer> t = new RedBlackTree<>();
            TreeSet<Integer> expected = new TreeSet<>();
            int range = 10 + rnd.nextInt(300);
            for (int step = 0; step < 2000; step++) {
                int v = rnd.nextInt(range);
                switch (rnd.nextInt(3)) {
                    case 0, 1 -> assertEquals(expected.add(v), t.add(v));
                    default -> assertEquals(expected.remove(v), t.remove(v));
                }
                t.checkInvariants();
            }
            assertEquals(new ArrayList<>(expected), t.inOrder());
            assertEquals(expected.size(), t.size());
        }
    }

    @Test
    public void worksWithOtherComparableTypes() {
        RedBlackTree<String> words = new RedBlackTree<>();
        for (String w : "груша яблуко вишня абрикос слива".split(" ")) words.add(w);
        words.checkInvariants();
        assertEquals(List.of("абрикос", "вишня", "груша", "слива", "яблуко"), words.inOrder());
    }

    //  сама перевірка властивостей 

    @Test
    public void checkInvariantsDetectsBrokenTree() throws Exception {
        addAll(10, 5, 15);
        // ламаємо дерево через рефлексію: робимо корінь червоним
        java.lang.reflect.Field color = RedBlackTree.Node.class.getDeclaredField("color");
        color.setAccessible(true);
        color.set(tree.root(), RedBlackTree.Color.RED);
        IllegalStateException e = assertThrows(IllegalStateException.class, tree::checkInvariants);
        assertTrue(e.getMessage().contains("корінь"));
    }
}
