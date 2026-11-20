package lab.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

/*
  Узагальнений список (версія 2.0 з Hard Task 2).
 
  Методи з першої версії залишились, тільки Object замінено на E.
  Нові методи показують, де потрібні wildcards:
   - "? extends E" - коли список ЧИТАЄ елементи з аргументу (producer);
   - "? super E"   - коли список ПЕРЕДАЄ свої елементи в аргумент (consumer);
   - "?"           - коли тип елементів аргументу взагалі не важливий.
 */
public interface MyList<E> extends Iterable<E> {

    // ---------- методи з умови Hard Task 2 ----------

    void add(E e);

    void add(int index, E element);

    void addAll(E[] c);

    void addAll(int index, E[] c);

    E get(int index);

    E remove(int index);

    void set(int index, E element);

    int indexOf(Object o);

    int size();

    Object[] toArray();

    // ---------- нове у версії 2.0 ----------

    // Як у java.util.List: у MyList<Number> можна додати Collection<Integer>
    boolean addAll(Collection<? extends E> c);

    // Те саме для наших власних списків
    boolean addAll(MyList<? extends E> c);

    boolean addAll(int index, MyList<? extends E> c);

    // Тип елементів c не важливий - порівнюємо через equals(Object)
    boolean containsAll(Collection<?> c);

    // Comparator<Object> чи Comparator<Number> теж підходить для MyList<Integer>
    void sort(Comparator<? super E> c);

    // Предикат для батьківського типу теж підходить
    boolean removeIf(Predicate<? super E> filter);

    // Масив потрібного типу (як у java.util.List)
    <T> T[] toArray(T[] a);

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
}
