package lab.collections;

import java.util.*;
import java.util.function.Predicate;

/*
  Список на основі масиву. Масив має тип Object[], бо new E[n] у Java створити не можна
  (через стирання типів E під час виконання невідомий). Тому при читанні робимо
  приведення (E) - воно безпечне, бо записати туди можна лише E через методи класу.
 */
public class MyArrayList<E> implements MyList<E>, RandomAccess {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] data;
    private int size;
    private int modCount; // для fail-fast ітератора

    public MyArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Ємність не може бути від'ємною: " + initialCapacity);
        }
        data = new Object[initialCapacity];
    }

    /** Копія з будь-якої колекції підтипів E. */
    public MyArrayList(Collection<? extends E> c) {
        this(Math.max(c.size(), DEFAULT_CAPACITY));
        Object[] arr = c.toArray();
        insertArray(0, arr, arr.length); // private-метод: не викликаємо перевизначуваний addAll з конструктора
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // масив elements тільки читаємо, назовні не віддаємо
    public static <E> MyArrayList<E> of(E... elements) {
        MyArrayList<E> list = new MyArrayList<>(elements.length);
        list.addAll(elements);
        return list;
    }

    @Override
    public void add(E e) {
        ensureCapacity(size + 1);
        data[size++] = e;
        modCount++;
    }

    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        ensureCapacity(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
        modCount++;
    }

    @Override
    public void addAll(E[] c) {
        addAll(size, c);
    }

    @Override
    public void addAll(int index, E[] c) {
        checkPositionIndex(index);
        Objects.requireNonNull(c, "Масив не може бути null");
        insertArray(index, c, c.length);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Objects.requireNonNull(c);
        Object[] arr = c.toArray(); // знімок - безпечно, навіть якщо c == цей список
        insertArray(size, arr, arr.length);
        return arr.length > 0;
    }

    @Override
    public boolean addAll(MyList<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, MyList<? extends E> c) {
        checkPositionIndex(index);
        Objects.requireNonNull(c);
        Object[] arr = c.toArray(); // list.addAll(list) теж працює
        insertArray(index, arr, arr.length);
        return arr.length > 0;
    }

    private void insertArray(int index, Object[] src, int n) {
        if (n == 0) {
            return;
        }
        ensureCapacity(size + n);
        System.arraycopy(data, index, data, index + n, size - index);
        System.arraycopy(src, 0, data, index, n);
        size += n;
        modCount++;
    }

    @Override
    public E get(int index) {
        checkElementIndex(index);
        return elementAt(index);
    }

    @Override
    public E remove(int index) {
        checkElementIndex(index);
        E old = elementAt(index);
        int tail = size - index - 1;
        if (tail > 0) {
            System.arraycopy(data, index + 1, data, index, tail);
        }
        data[--size] = null;
        modCount++;
        return old;
    }

    @Override
    public void set(int index, E element) {
        checkElementIndex(index);
        data[index] = element;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(o, data[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (indexOf(o) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        Objects.requireNonNull(c);
        Sorting.<E>mergeSort(data, size, c);
        modCount++;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // один прохід: зсуваємо "живі" елементи вліво, O(n), а не O(n^2)
        int w = 0;
        for (int r = 0; r < size; r++) {
            E e = elementAt(r);
            if (!filter.test(e)) {
                data[w++] = e;
            }
        }
        boolean changed = w != size;
        for (int i = w; i < size; i++) {
            data[i] = null;
        }
        size = w;
        if (changed) modCount++;
        return changed;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] copy = new Object[size];
        System.arraycopy(data, 0, copy, 0, size);
        return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            // створюємо масив того ж типу, що й a (тип береться під час виконання)
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        System.arraycopy(data, 0, a, 0, size); // ArrayStoreException, якщо тип не підходить
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int cursor;
            private int lastRet = -1;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public E next() {
                checkForComodification();
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                lastRet = cursor++;
                return elementAt(lastRet);
            }

            @Override
            public void remove() {
                if (lastRet < 0) {
                    throw new IllegalStateException();
                }
                checkForComodification();
                MyArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            }

            private void checkForComodification() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    private E elementAt(int i) {
        return (E) data[i];
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }
        int newCapacity = Math.max(data.length + (data.length >> 1), minCapacity);
        newCapacity = Math.max(newCapacity, DEFAULT_CAPACITY);
        Object[] bigger = new Object[newCapacity];
        System.arraycopy(data, 0, bigger, 0, size);
        data = bigger;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(data[i]);
        }
        return sb.append(']').toString();
    }
}
