package lab.collections;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/*
  Допоміжні методи для MyList - аналог java.util.Collections.
  Тут wildcards працюють найбільше, бо методи мають бути максимально
  "гнучкими" щодо типів аргументів. Правило PECS (Producer Extends, Consumer Super).
 */
public final class MyCollections {

    private MyCollections() {
    }

    /*
      src - тільки ЧИТАЄМО (producer) -> ? extends T,
      dest - тільки ЗАПИСУЄМО (consumer) -> ? super T.
      Завдяки цьому можна скопіювати MyList<Integer> у MyList<Number> чи MyList<Object>.
     */
    public static <T> void copy(MyList<? super T> dest, MyList<? extends T> src) {
        for (T e : src) {
            dest.add(e);
        }
    }

    // Будь-які числа: MyList<Integer>, MyList<Double>, MyList<Number>...
    public static double sum(MyList<? extends Number> list) {
        double s = 0;
        for (Number n : list) {
            s += n.doubleValue();
        }
        return s;
    }

    /*
      Максимум з "природним" порядком. Comparable<? super T> потрібен для класів,
      що успадковують compareTo від батька (наприклад, java.sql.Timestamp extends Date
      implements Comparable<Date>).
     */
    public static <T extends Comparable<? super T>> T max(MyList<? extends T> list) {
        return max(list, Comparator.naturalOrder());
    }

    public static <T> T max(MyList<? extends T> list, Comparator<? super T> c) {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Порожній список");
        }
        T best = list.get(0);
        for (T e : list) {
            if (c.compare(e, best) > 0) {
                best = e;
            }
        }
        return best;
    }

    // Нам байдуже, що всередині - тільки кількість і toString. Тому просто "?". */
    public static String describe(MyList<?> list) {
        return list.getClass().getSimpleName() + " (" + list.size() + " ел.): " + list;
    }

    // Нові елементи потрібного типу кладемо в будь-який список, що може їх прийняти. */
    public static <T> void fill(MyList<? super T> dest, int count, T value) {
        for (int i = 0; i < count; i++) {
            dest.add(value);
        }
    }

    /*
      Перетворення елементів: функція приймає "щонайменше" T і повертає "щонайбільше" R.
      Результат - новий MyArrayList<R>.
     */
    public static <T, R> MyList<R> map(MyList<? extends T> src, Function<? super T, ? extends R> f) {
        MyList<R> result = new MyArrayList<>(src.size());
        for (T e : src) {
            result.add(f.apply(e));
        }
        return result;
    }

    public static <T> MyList<T> filter(MyList<? extends T> src, Predicate<? super T> p) {
        MyList<T> result = new MyArrayList<>();
        for (T e : src) {
            if (p.test(e)) {
                result.add(e);
            }
        }
        return result;
    }

    // Чи є хоча б один спільний елемент. Типи списків можуть бути зовсім різні.
    public static boolean disjoint(MyList<?> a, MyList<?> b) {
        for (Object o : a) {
            if (b.contains(o)) {
                return false;
            }
        }
        return true;
    }
}
