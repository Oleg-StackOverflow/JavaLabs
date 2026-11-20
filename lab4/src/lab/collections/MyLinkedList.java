package lab.collections;

import java.util.*;
import java.util.function.Predicate;

/*
  Двозв'язний список. Вставка/видалення на кінцях - O(1),
  доступ за індексом - O(n), тому RandomAccess тут НЕ реалізується.
 */
public class MyLinkedList<E> implements MyList<E> {

    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;

        Node(Node<E> prev, E item, Node<E> next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

    private Node<E> first;
    private Node<E> last;
    private int size;
    private int modCount;

    public MyLinkedList() {
    }

    public MyLinkedList(Collection<? extends E> c) {
        insertArray(0, c.toArray()); // private-метод: не викликаємо перевизначуваний addAll з конструктора
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // масив elements тільки читаємо, назовні не віддаємо
    public static <E> MyLinkedList<E> of(E... elements) {
        MyLinkedList<E> list = new MyLinkedList<>();
        list.addAll(elements);
        return list;
    }

    @Override
    public void add(E e) {
        linkLast(e);
    }

    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
    }

    @Override
    public void addAll(E[] c) {
        addAll(size, c);
    }

    @Override
    public void addAll(int index, E[] c) {
        checkPositionIndex(index);
        Objects.requireNonNull(c, "Масив не може бути null");
        insertArray(index, c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Objects.requireNonNull(c);
        return insertArray(size, c.toArray());
    }

    @Override
    public boolean addAll(MyList<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, MyList<? extends E> c) {
        checkPositionIndex(index);
        Objects.requireNonNull(c);
        return insertArray(index, c.toArray()); // знімок: list.addAll(list) не зациклиться
    }

    // Знаходимо місце один раз і пришиваємо всі нові вузли ланцюжком.
    @SuppressWarnings("unchecked")
    private boolean insertArray(int index, Object[] arr) {
        if (arr.length == 0) {
            return false;
        }
        Node<E> succ = (index == size) ? null : node(index);
        Node<E> pred = (succ == null) ? last : succ.prev;

        for (Object o : arr) {
            Node<E> n = new Node<>(pred, (E) o, null); // елементи прийшли з E[] або ? extends E
            if (pred == null) {
                first = n;
            } else {
                pred.next = n;
            }
            pred = n;
        }
        if (succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }
        size += arr.length;
        modCount++;
        return true;
    }

    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    @Override
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    @Override
    public void set(int index, E element) {
        checkElementIndex(index);
        node(index).item = element;
    }

    @Override
    public int indexOf(Object o) {
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next, i++) {
            if (Objects.equals(o, x.item)) {
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

    // Копіюємо в масив, сортуємо злиттям, записуємо назад у ті самі вузли.
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        Objects.requireNonNull(c);
        Object[] arr = toArray();
        Sorting.<E>mergeSort(arr, arr.length, c);
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next) {
            x.item = (E) arr[i++];
        }
        modCount++;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean changed = false;
        Node<E> x = first;
        while (x != null) {
            Node<E> next = x.next;
            if (filter.test(x.item)) {
                unlink(x);
                changed = true;
            }
            x = next;
        }
        return changed;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next) {
            result[i++] = x.item;
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        Object[] result = a; // запис через Object[] - JVM сама перевірить тип (ArrayStoreException)
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next) {
            result[i++] = x.item;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private Node<E> next = first;
            private Node<E> lastReturned;
            private int expectedModCount = modCount;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public E next() {
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (next == null) {
                    throw new NoSuchElementException();
                }
                lastReturned = next;
                next = next.next;
                return lastReturned.item;
            }

            @Override
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                if (modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                unlink(lastReturned);
                lastReturned = null;
                expectedModCount = modCount;
            }
        };
    }

    // ---------- внутрішні операції з вузлами ----------

    private void linkLast(E e) {
        Node<E> l = last;
        Node<E> n = new Node<>(l, e, null);
        last = n;
        if (l == null) {
            first = n;
        } else {
            l.next = n;
        }
        size++;
        modCount++;
    }

    private void linkBefore(E e, Node<E> succ) {
        Node<E> pred = succ.prev;
        Node<E> n = new Node<>(pred, e, succ);
        succ.prev = n;
        if (pred == null) {
            first = n;
        } else {
            pred.next = n;
        }
        size++;
        modCount++;
    }

    private E unlink(Node<E> x) {
        E item = x.item;
        Node<E> prev = x.prev;
        Node<E> next = x.next;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
        }
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
        x.item = null;
        x.prev = null;
        x.next = null;
        size--;
        modCount++;
        return item;
    }

    private Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++) x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--) x = x.prev;
            return x;
        }
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
        for (Node<E> x = first; x != null; x = x.next) {
            sb.append(x.item);
            if (x.next != null) sb.append(", ");
        }
        return sb.append(']').toString();
    }
}
