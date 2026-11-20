package lab.collections;

import java.util.RandomAccess;

/*
 Список на основі масиву. Доступ за індексом - O(1), тому реалізує
 маркерний інтерфейс RandomAccess , алгоритми можуть перевірити його через
 instanceof і обрати цикл по індексу замість ітератора
 */
public class MyArrayList implements MyList, RandomAccess {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] data;
    private int size;

    public MyArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Ємність не може бути від'ємною: " + initialCapacity);
        }
        data = new Object[initialCapacity];
    }

    @Override
    public void add(Object e) {
        ensureCapacity(size + 1);
        data[size++] = e;
    }

    @Override
    public void add(int index, Object element) {
        checkPositionIndex(index);
        ensureCapacity(size + 1);
        // зсуваємо хвіст на одну позицію вправо
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }

    @Override
    public void addAll(Object[] c) {
        addAll(size, c);
    }

    @Override
    public void addAll(int index, Object[] c) {
        checkPositionIndex(index);
        if (c == null) {
            throw new NullPointerException("Масив не може бути null");
        }
        int n = c.length;
        if (n == 0) {
            return;
        }
        ensureCapacity(size + n);
        System.arraycopy(data, index, data, index + n, size - index);
        System.arraycopy(c, 0, data, index, n);
        size += n;
    }

    @Override
    public Object get(int index) {
        checkElementIndex(index);
        return data[index];
    }

    @Override
    public Object remove(int index) {
        checkElementIndex(index);
        Object old = data[index];
        int tail = size - index - 1;
        if (tail > 0) {
            System.arraycopy(data, index + 1, data, index, tail);
        }
        data[--size] = null; // щоб не тримати посилання і не заважати GC
        return old;
    }

    @Override
    public void set(int index, Object element) {
        checkElementIndex(index);
        data[index] = element;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == null ? data[i] == null : o.equals(data[i])) {
                return i;
            }
        }
        return -1;
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

    // Збільшуємо масив у 1.5 рази (як у ArrayList), але не менше ніж потрібно.
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

    // для get/set/remove: 0..size-1
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    // для вставки: 0..size (можна вставити в самий кінець)
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
