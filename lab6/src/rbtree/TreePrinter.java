package rbtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  Відображення дерева в консолі. Колір позначається дужками, щоб його було видно
  в будь-якій консолі, без кольорового виводу:
    [13] - чорний вузол,   (8) - червоний вузол.
 */
public final class TreePrinter {

    public static final String LEGEND = "[x] - чорний вузол, (x) - червоний вузол";

    private TreePrinter() {
    }

    public static <K extends Comparable<? super K>> String label(RedBlackTree.Node<K> n) {
        return n.isRed() ? "(" + n.key() + ")" : "[" + n.key() + "]";
    }

    /*
      Дерево зверху вниз. Кожен вузол займає свою колонку за порядком in-order,
      тому дерево ніколи не "налазить" саме на себе:
     
                 [4]
           ┌──────┴──────┐
          (2)           (6)
        ┌──┴──┐       ┌──┴──┐
       [1]   [3]     [5]   [7]
     */
    public static <K extends Comparable<? super K>> String topDown(RedBlackTree<K> tree) {
        RedBlackTree.Node<K> root = tree.root();
        if (root == null) return "(порожнє дерево)\n";

        // in-order: номер колонки + глибина кожного вузла
        List<RedBlackTree.Node<K>> order = new ArrayList<>();
        Map<RedBlackTree.Node<K>, Integer> depth = new HashMap<>();
        collect(tree, root, 0, order, depth);

        int cellW = 0;
        for (RedBlackTree.Node<K> n : order) cellW = Math.max(cellW, label(n).length());
        cellW += 1;                                     // мінімум один пробіл між сусідами
        int width = order.size() * cellW;
        Map<RedBlackTree.Node<K>, Integer> col = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            col.put(order.get(i), i * cellW + cellW / 2);
        }

        int levels = tree.height();
        StringBuilder sb = new StringBuilder();
        for (int d = 0; d < levels; d++) {
            char[] labels = blank(width);
            char[] links = blank(width);
            boolean anyLinks = false;
            for (RedBlackTree.Node<K> n : order) {
                if (depth.get(n) != d) continue;
                String text = label(n);
                int c = col.get(n);
                int start = Math.max(0, c - (text.length() - 1) / 2);
                text.getChars(0, text.length(), labels, start);

                RedBlackTree.Node<K> l = tree.left(n), r = tree.right(n);
                if (l == null && r == null) continue;
                anyLinks = true;
                int from = l != null ? col.get(l) : c;
                int to = r != null ? col.get(r) : c;
                for (int i = from; i <= to; i++) links[i] = '─';
                if (l != null) links[from] = '┌';
                if (r != null) links[to] = '┐';
                links[c] = (l != null && r != null) ? '┴' : (l != null ? '┘' : '└');
            }
            sb.append(rtrim(labels)).append('\n');
            if (anyLinks) sb.append(rtrim(links)).append('\n');
        }
        return sb.toString();
    }

    // Ширина рисунка topDown у символах - щоб вирішити, чи влізе він у консоль
    public static <K extends Comparable<? super K>> int topDownWidth(RedBlackTree<K> tree) {
        int maxLabel = 0;
        for (K k : tree.inOrder()) maxLabel = Math.max(maxLabel, String.valueOf(k).length() + 2);
        return tree.size() * (maxLabel + 1);
    }

    /*
      Дерево "боком" (корінь зліва, праве піддерево зверху) - для великих дерев,
      які не влазять у ширину консолі. Щоб прочитати, нахиліть голову вліво.
     */
    public static <K extends Comparable<? super K>> String sideways(RedBlackTree<K> tree) {
        RedBlackTree.Node<K> root = tree.root();
        if (root == null) return "(порожнє дерево)\n";
        StringBuilder sb = new StringBuilder();
        side(tree, root, "", 0, sb);
        return sb.toString();
    }

    // kind: 0 - корінь, 1 - верхня (права) дитина, 2 - нижня (ліва) дитина
    private static <K extends Comparable<? super K>> void side(RedBlackTree<K> tree, RedBlackTree.Node<K> n,
                                                                String prefix, int kind, StringBuilder sb) {
        RedBlackTree.Node<K> r = tree.right(n), l = tree.left(n);
        if (r != null) {
            side(tree, r, prefix + (kind == 2 ? "│   " : "    "), 1, sb);
        }
        String connector = kind == 0 ? "─── " : (kind == 1 ? "┌── " : "└── ");
        sb.append(prefix).append(connector).append(label(n)).append('\n');
        if (l != null) {
            side(tree, l, prefix + (kind == 1 ? "│   " : "    "), 2, sb);
        }
    }

    // Автоматичний вибір: зверху вниз, якщо влазить у maxWidth символів, інакше боком
    public static <K extends Comparable<? super K>> String render(RedBlackTree<K> tree, int maxWidth) {
        return topDownWidth(tree) <= maxWidth ? topDown(tree) : sideways(tree);
    }

    private static <K extends Comparable<? super K>> void collect(RedBlackTree<K> tree, RedBlackTree.Node<K> n, int d,
                                                                   List<RedBlackTree.Node<K>> order,
                                                                   Map<RedBlackTree.Node<K>, Integer> depth) {
        if (n == null) return;
        collect(tree, tree.left(n), d + 1, order, depth);
        order.add(n);
        depth.put(n, d);
        collect(tree, tree.right(n), d + 1, order, depth);
    }

    private static char[] blank(int n) {
        char[] a = new char[n];
        Arrays.fill(a, ' ');
        return a;
    }

    private static String rtrim(char[] a) {
        int end = a.length;
        while (end > 0 && a[end - 1] == ' ') end--;
        return new String(a, 0, end);
    }
}
