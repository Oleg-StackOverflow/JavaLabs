package rbtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/*
 Червоно-чорне дерево пошуку (за алгоритмами з підручника Кормена, CLRS, гл. 13).
 Властивості, які дерево підтримує після кожної операції:
 1. кожен вузол червоний або чорний;
 2. корінь чорний;
 3. кожен лист (NIL) чорний;
 4. у червоного вузла обидва діти чорні бо двох червоних підряд не буває;
 5. на будь-якому шляху від вузла до листків однакова кількість чорних вузлів.
 Звідси висота дерева не більша за 2*log2(n+1), тож пошук/вставка/видалення - O(log n).
 Замість null використовується один спільний вартовий вузол NIL (чорний).
 Так у fixup-методах не треба окремо перевіряти null.
 Дублікати не зберігаються (поведінка як у множини): повторне add повертає false.
 */
public class RedBlackTree<K extends Comparable<? super K>> {

    public enum Color { RED, BLACK }

    // Вузол. Відкритий лише для читання (для TreePrinter і тестів), змінює його тільки дерево.
    public static final class Node<K> {
        private K key;
        private Color color;
        private Node<K> left, right, parent;

        private Node(K key, Color color) {
            this.key = key;
            this.color = color;
        }

        public K key() { return key; }
        public Color color() { return color; }
        public boolean isRed() { return color == Color.RED; }
    }

    private final Node<K> nil = new Node<>(null, Color.BLACK);
    private Node<K> root = nil;
    private int size;

    public RedBlackTree() {
        nil.left = nil.right = nil.parent = nil;
    }

    // Пошук

    public boolean contains(K key) {
        return find(key) != nil;
    }

    private Node<K> find(K key) {
        Objects.requireNonNull(key, "key");
        Node<K> x = root;
        while (x != nil) {
            int c = key.compareTo(x.key);
            if (c == 0) return x;
            x = c < 0 ? x.left : x.right;
        }
        return nil;
    }

    // Додавання

    // @return true, якщо ключ додано; false, якщо він уже був у дереві.
    public boolean add(K key) {
        Objects.requireNonNull(key, "key");
        Node<K> parent = nil;
        Node<K> x = root;
        int c = 0;
        while (x != nil) {                       // звичайний спуск як у BST
            parent = x;
            c = key.compareTo(x.key);
            if (c == 0) return false;
            x = c < 0 ? x.left : x.right;
        }
        Node<K> z = new Node<>(key, Color.RED);  // новий вузол завжди червоний
        z.left = z.right = nil;
        z.parent = parent;
        if (parent == nil) {
            root = z;
        } else if (c < 0) {
            parent.left = z;
        } else {
            parent.right = z;
        }
        size++;
        insertFixup(z);
        return true;
    }

    /*
     Після вставки червоного z може порушитись лише властивість 4 (червоний батько)
     або 2 (z - корінь). Три випадки (+ дзеркальні):
      1) дядько червоний  -> перефарбувати батька, дядька, діда; підійматись вище;
      2) дядько чорний, z - "внутрішній" онук -> поворот навколо батька, переходимо до 3;
      3) дядько чорний, z - "зовнішній" онук  -> перефарбувати + поворот навколо діда.
     */
    private void insertFixup(Node<K> z) {
        while (z.parent.isRed()) {
            Node<K> gp = z.parent.parent;
            if (z.parent == gp.left) {
                Node<K> uncle = gp.right;
                if (uncle.isRed()) {                                  // випадок 1
                    z.parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    gp.color = Color.RED;
                    z = gp;
                } else {
                    if (z == z.parent.right) {                        // випадок 2
                        z = z.parent;
                        rotateLeft(z);
                    }
                    z.parent.color = Color.BLACK;                     // випадок 3
                    gp.color = Color.RED;
                    rotateRight(gp);
                }
            } else {                                                  // дзеркально
                Node<K> uncle = gp.left;
                if (uncle.isRed()) {
                    z.parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    gp.color = Color.RED;
                    z = gp;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rotateRight(z);
                    }
                    z.parent.color = Color.BLACK;
                    gp.color = Color.RED;
                    rotateLeft(gp);
                }
            }
        }
        root.color = Color.BLACK;
    }

    // Видалення

    // @return true, якщо ключ був і його видалено
    public boolean remove(K key) {
        Node<K> z = find(key);
        if (z == nil) {
            return false;
        }
        Node<K> y = z;                 // вузол, який фізично зникне з дерева
        Color yOriginalColor = y.color;
        Node<K> x;                     // вузол, що стане на місце y
        if (z.left == nil) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == nil) {
            x = z.left;
            transplant(z, z.left);
        } else {
            y = minimum(z.right);      // наступник: найменший у правому піддереві
            yOriginalColor = y.color;
            x = y.right;
            if (y.parent == z) {
                x.parent = y;          // важливо, навіть якщо x == nil
            } else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            y.color = z.color;
        }
        size--;
        if (yOriginalColor == Color.BLACK) {
            deleteFixup(x);            // зник чорний вузол -> на якомусь шляху "бракує" чорного
        }
        nil.parent = nil;              // прибираємо слід, який transplant міг залишити у вартового
        return true;
    }

    /*
     x несе "зайвий чорний". Чотири випадки (+ дзеркальні), w - брат x:
      1) w червоний -> перефарбувати і повернути, щоб брат став чорним;
      2) w чорний, обидва діти w чорні -> w червоний, "зайвий чорний" іде вгору;
      3) w чорний, ближній дитина червона, дальній чорна -> поворот навколо w, до випадку 4;
      4) w чорний, дальній дитина червона -> перефарбувати + поворот навколо батька, кінець.
     */
    private void deleteFixup(Node<K> x) {
        while (x != root && x.color == Color.BLACK) {
            if (x == x.parent.left) {
                Node<K> w = x.parent.right;
                if (w.isRed()) {                                              // 1
                    w.color = Color.BLACK;
                    x.parent.color = Color.RED;
                    rotateLeft(x.parent);
                    w = x.parent.right;
                }
                if (!w.left.isRed() && !w.right.isRed()) {                    // 2
                    w.color = Color.RED;
                    x = x.parent;
                } else {
                    if (!w.right.isRed()) {                                   // 3
                        w.left.color = Color.BLACK;
                        w.color = Color.RED;
                        rotateRight(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;                                 // 4
                    x.parent.color = Color.BLACK;
                    w.right.color = Color.BLACK;
                    rotateLeft(x.parent);
                    x = root;
                }
            } else {                                                          // дзеркально
                Node<K> w = x.parent.left;
                if (w.isRed()) {
                    w.color = Color.BLACK;
                    x.parent.color = Color.RED;
                    rotateRight(x.parent);
                    w = x.parent.left;
                }
                if (!w.right.isRed() && !w.left.isRed()) {
                    w.color = Color.RED;
                    x = x.parent;
                } else {
                    if (!w.left.isRed()) {
                        w.right.color = Color.BLACK;
                        w.color = Color.RED;
                        rotateLeft(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = Color.BLACK;
                    w.left.color = Color.BLACK;
                    rotateRight(x.parent);
                    x = root;
                }
            }
        }
        x.color = Color.BLACK;
    }

    // Ставить піддерево v на місце піддерева u
    private void transplant(Node<K> u, Node<K> v) {
        if (u.parent == nil) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;           // для v == nil теж: deleteFixup потрібен x.parent
    }

    private Node<K> minimum(Node<K> x) {
        while (x.left != nil) x = x.left;
        return x;
    }

    // Повороти

    /*
         x                y
        / \              / \
       a   y     ->     x   c
          / \          / \
         b   c        a   b
     */
    private void rotateLeft(Node<K> x) {
        Node<K> y = x.right;
        x.right = y.left;
        if (y.left != nil) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node<K> x) {
        Node<K> y = x.left;
        x.left = y.right;
        if (y.right != nil) y.right.parent = x;
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
    }

    // Обходи

    // Симетричний (in-order): ключі у порядку зростання.
    public List<K> inOrder() {
        List<K> out = new ArrayList<>(size);
        Deque<Node<K>> stack = new ArrayDeque<>();   // без рекурсії - не впаде на великих деревах
        Node<K> x = root;
        while (x != nil || !stack.isEmpty()) {
            while (x != nil) {
                stack.push(x);
                x = x.left;
            }
            x = stack.pop();
            out.add(x.key);
            x = x.right;
        }
        return out;
    }

    // Прямий (pre-order): корінь, ліве, праве
    public List<K> preOrder() {
        List<K> out = new ArrayList<>(size);
        if (root == nil) return out;
        Deque<Node<K>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node<K> x = stack.pop();
            out.add(x.key);
            if (x.right != nil) stack.push(x.right);   // праве кладемо першим - дістанемо другим
            if (x.left != nil) stack.push(x.left);
        }
        return out;
    }

    // Зворотний (post-order): ліве, праве, корінь
    public List<K> postOrder() {
        List<K> out = new ArrayList<>(size);
        postOrder(root, out::add);
        return out;
    }

    private void postOrder(Node<K> x, Consumer<K> visit) {
        if (x == nil) return;                           // глибина рекурсії <= висота = O(log n)
        postOrder(x.left, visit);
        postOrder(x.right, visit);
        visit.accept(x.key);
    }

    // У ширину (по рівнях): кожен внутрішній список - один рівень
    public List<List<K>> levelOrder() {
        List<List<K>> levels = new ArrayList<>();
        if (root == nil) return levels;
        List<Node<K>> current = List.of(root);
        while (!current.isEmpty()) {
            List<K> keys = new ArrayList<>();
            List<Node<K>> next = new ArrayList<>();
            for (Node<K> x : current) {
                keys.add(x.key);
                if (x.left != nil) next.add(x.left);
                if (x.right != nil) next.add(x.right);
            }
            levels.add(keys);
            current = next;
        }
        return levels;
    }

    // Інформація про дерево

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public void clear() {
        root = nil;
        size = 0;
    }

    // Кількість вузлів на найдовшому шляху від кореня до листка (порожнє дерево - 0)
    public int height() {
        return height(root);
    }

    private int height(Node<K> x) {
        return x == nil ? 0 : 1 + Math.max(height(x.left), height(x.right));
    }

    // Чорна висота: кількість чорних вузлів на шляху від кореня до листка, включно з коренем, без NIL
    public int blackHeight() {
        int bh = 0;
        for (Node<K> x = root; x != nil; x = x.left) {
            if (x.color == Color.BLACK) bh++;
        }
        return bh;
    }

    // Корінь для відображення; null - якщо дерево порожнє
    public Node<K> root() {
        return root == nil ? null : root;
    }

    public Node<K> left(Node<K> x) {
        return x.left == nil ? null : x.left;
    }

    public Node<K> right(Node<K> x) {
        return x.right == nil ? null : x.right;
    }

    /*
      Перевірка всіх п'яти властивостей + властивості дерева пошуку + коректності
      посилань на батьків. Кидає IllegalStateException з поясненням, що саме зламано.
      Використовується в тестах після кожної операції.
     */
    public void checkInvariants() {
        if (root.color != Color.BLACK) {
            throw new IllegalStateException("корінь не чорний");
        }
        if (nil.color != Color.BLACK) {
            throw new IllegalStateException("NIL не чорний");
        }
        if (root != nil && root.parent != nil) {
            throw new IllegalStateException("у кореня є батько");
        }
        int[] count = {0};
        checkNode(root, null, null, count);
        if (count[0] != size) {
            throw new IllegalStateException("size=" + size + ", а вузлів " + count[0]);
        }
    }

    // @return чорна висота піддерева x (враховуючи NIL).
    private int checkNode(Node<K> x, K low, K high, int[] count) {
        if (x == nil) return 1;
        count[0]++;
        if ((low != null && x.key.compareTo(low) <= 0) || (high != null && x.key.compareTo(high) >= 0)) {
            throw new IllegalStateException("порушено порядок BST біля ключа " + x.key);
        }
        if (x.isRed() && (x.left.isRed() || x.right.isRed())) {
            throw new IllegalStateException("червоний вузол " + x.key + " має червону дитину");
        }
        if (x.left != nil && x.left.parent != x || x.right != nil && x.right.parent != x) {
            throw new IllegalStateException("зіпсоване посилання на батька біля " + x.key);
        }
        int lh = checkNode(x.left, low, x.key, count);
        int rh = checkNode(x.right, x.key, high, count);
        if (lh != rh) {
            throw new IllegalStateException("різна чорна висота під вузлом " + x.key + ": " + lh + " і " + rh);
        }
        return lh + (x.isRed() ? 0 : 1);
    }

    @Override
    public String toString() {
        return inOrder().toString();
    }
}
