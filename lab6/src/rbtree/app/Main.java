package rbtree.app;

import rbtree.RedBlackTree;
import rbtree.TreePrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/*
 Hard Task 6: червоно-чорне дерево в консолі.
 Без аргументів - меню; з аргументом --demo - готовий сценарій
 */
public class Main {

    /** Ширина консолі, в яку має влізти дерево "зверху вниз"; ширші дерева друкуються боком. */
    private static final int MAX_WIDTH = 120;

    private final Scanner in;
    private final Random random;
    private RedBlackTree<Integer> tree = new RedBlackTree<>();

    Main(Scanner in, Random random) {
        this.in = in;
        this.random = random;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--demo")) {
            new Main(null, new Random(2026)).demo();   // фіксований seed - однаковий лог при кожному запуску
        } else {
            new Main(new Scanner(System.in), new Random()).menu();
        }
    }

    // Меню

    private void menu() {
        System.out.println("Червоно-чорне дерево. " + TreePrinter.LEGEND);
        while (true) {
            System.out.println();
            System.out.println("1 - заповнити випадковими числами (у довільному порядку)");
            System.out.println("2 - заповнити випадковими числами, впорядкованими за зростанням");
            System.out.println("3 - додати числа з клавіатури");
            System.out.println("4 - видалити числа");
            System.out.println("5 - знайти число");
            System.out.println("6 - показати дерево");
            System.out.println("7 - обходи дерева");
            System.out.println("8 - очистити дерево");
            System.out.println("0 - вихід");
            String choice;
            try {
                choice = ask("> ");
            } catch (NoSuchElementException e) {
                return;                                 // кінець вводу (Ctrl+Z / Ctrl+D)
            }
            try {
                switch (choice) {
                    case "1" -> fillRandom(false);
                    case "2" -> fillRandom(true);
                    case "3" -> addFromKeyboard();
                    case "4" -> removeFromKeyboard();
                    case "5" -> search();
                    case "6" -> show();
                    case "7" -> traversals();
                    case "8" -> {
                        tree.clear();
                        System.out.println("Дерево очищено.");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Немає такого пункту.");
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    private void fillRandom(boolean sorted) {
        int n = askInt("Скільки чисел (1..200): ", 1, 200);
        int[] arr = randomDistinct(n);
        if (sorted) {
            Arrays.sort(arr);
        }
        System.out.println("Масив (порядок додавання): " + Arrays.toString(arr));
        tree = new RedBlackTree<>();
        for (int v : arr) {
            tree.add(v);
        }
        show();
    }

    private void addFromKeyboard() {
        List<Integer> nums = askNumbers("Числа через пробіл або кому: ");
        int added = 0;
        for (int v : nums) {
            if (tree.add(v)) {
                added++;
            } else {
                System.out.println("  " + v + " уже є в дереві - пропущено");
            }
        }
        System.out.println("Додано: " + added);
        show();
    }

    private void removeFromKeyboard() {
        if (tree.isEmpty()) {
            System.out.println("Дерево порожнє.");
            return;
        }
        List<Integer> nums = askNumbers("Які числа видалити: ");
        for (int v : nums) {
            System.out.println("  " + v + (tree.remove(v) ? " - видалено" : " - немає в дереві"));
        }
        show();
    }

    private void search() {
        List<Integer> nums = askNumbers("Яке число шукати: ");
        for (int v : nums) {
            System.out.println("  " + v + (tree.contains(v) ? " - є в дереві" : " - немає в дереві"));
        }
    }

    // Демо-сценарій

    private void demo() {
        System.out.println("Червоно-чорне дерево. " + TreePrinter.LEGEND);

        title("1. Випадкові числа в довільному порядку");
        int[] arr = randomDistinct(15);
        System.out.println("Масив (порядок додавання): " + Arrays.toString(arr));
        for (int v : arr) tree.add(v);
        show();
        traversals();

        title("2. Ті самі числа, впорядковані за зростанням");
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        System.out.println("Масив (порядок додавання): " + Arrays.toString(sorted));
        RedBlackTree<Integer> randomTree = tree;
        tree = new RedBlackTree<>();
        for (int v : sorted) tree.add(v);
        show();
        System.out.println("Звичайне BST з тих самих впорядкованих чисел виродилось би в список висотою " + sorted.length + ".");

        title("3. Додавання з клавіатури (імітація введення \"50, 7 x 7\")");
        tree = randomTree;
        for (String token : "50, 7 x 7".split("[\\s,;]+")) {
            try {
                int v = Integer.parseInt(token);
                System.out.println("  " + v + (tree.add(v) ? " - додано" : " - уже є в дереві, пропущено"));
            } catch (NumberFormatException e) {
                System.out.println("  \"" + token + "\" - не ціле число, пропущено");
            }
        }
        show();

        title("4. Видалення");
        Integer rootKey = tree.root().key();
        for (int v : new int[]{rootKey, arr[0], 999}) {
            System.out.println("remove(" + v + ")" + (v == rootKey ? " - корінь" : "") + ": "
                    + (tree.remove(v) ? "видалено" : "немає в дереві"));
        }
        show();

        title("5. Дерево з 30 чисел (1..30) - не влазить у ширину, тому друкується боком");
        tree = new RedBlackTree<>();
        for (int i = 1; i <= 30; i++) tree.add(i);
        show();

        title("6. Велике дерево: 1000 чисел по зростанню");
        RedBlackTree<Integer> big = new RedBlackTree<>();
        for (int i = 1; i <= 1000; i++) big.add(i);
        System.out.println(stats(big));
        big.checkInvariants();
        System.out.println("Усі 5 властивостей червоно-чорного дерева виконуються.");
    }

    // Вивід

    private void show() {
        System.out.print(TreePrinter.render(tree, MAX_WIDTH));
        System.out.println(stats(tree));
    }

    private void traversals() {
        if (tree.isEmpty()) {
            System.out.println("Дерево порожнє.");
            return;
        }
        System.out.println("in-order   (ліве, корінь, праве): " + tree.inOrder());
        System.out.println("pre-order  (корінь, ліве, праве): " + tree.preOrder());
        System.out.println("post-order (ліве, праве, корінь): " + tree.postOrder());
        System.out.println("по рівнях (у ширину):             " + tree.levelOrder());
    }

    static String stats(RedBlackTree<?> t) {
        if (t.isEmpty()) return "Вузлів: 0";
        double bound = 2 * Math.log(t.size() + 1) / Math.log(2);
        return String.format("Вузлів: %d, висота: %d, чорна висота: %d, межа 2*log2(n+1) = %.1f",
                t.size(), t.height(), t.blackHeight(), bound);
    }

    private static void title(String s) {
        System.out.println();
        System.out.println("=== " + s + " ===");
    }

    // Введення

    // n різних випадкових чисел. Діапазон росте з n, щоб числа не були "впритул"
    private int[] randomDistinct(int n) {
        int max = Math.max(99, n * 5);
        List<Integer> pool = new ArrayList<>(max);
        for (int i = 1; i <= max; i++) pool.add(i);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = pool.remove(random.nextInt(pool.size()));   // без повторів
        }
        return arr;
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            String s = ask(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("  Потрібне число від " + min + " до " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  \"" + s + "\" - не ціле число.");
            }
        }
    }

    // Список цілих чисел; неправильні фрагменти пропускаються з повідомленням. Порожнього списку не повертає.
    private List<Integer> askNumbers(String prompt) {
        while (true) {
            List<Integer> result = new ArrayList<>();
            for (String token : ask(prompt).split("[\\s,;]+")) {
                if (token.isEmpty()) continue;
                try {
                    result.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    System.out.println("  \"" + token + "\" - не ціле число, пропущено");
                }
            }
            if (!result.isEmpty()) return result;
            System.out.println("  Не введено жодного числа.");
        }
    }
}
