package lab.collections;

/*
 * Двозв'язний список. Вставка/видалення на кінцях - O(1),
 * доступ за індексом - O(n), тому RandomAccess тут НЕ реалізується.
 */
public class MyLinkedList implements MyList {

    private static class Node {
        Object item;
        Node prev;
        Node next;

        Node(Node prev, Object item, Node next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

    private Node first;
    private Node last;
    private int size;

    @Override
    public void add(Object e) {
        linkLast(e);
    }

    @Override
    public void add(int index, Object element) {
        checkPositionIndex(index);
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
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
        if (c.length == 0) {
            return;
        }
        // знаходимо вузол, перед яким вставляємо, один раз, а не для кожного елемента
        Node succ = (index == size) ? null : node(index);
        Node pred = (succ == null) ? last : succ.prev;

        for (Object o : c) {
            Node n = new Node(pred, o, null);
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
        size += c.length;
    }

    @Override
    public Object get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    @Override
    public Object remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    @Override
    public void set(int index, Object element) {
        checkElementIndex(index);
        node(index).item = element;
    }

    @Override
    public int indexOf(Object o) {
        int i = 0;
        for (Node x = first; x != null; x = x.next, i++) {
            if (o == null ? x.item == null : o.equals(x.item)) {
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
        Object[] result = new Object[size];
        int i = 0;
        for (Node x = first; x != null; x = x.next) {
            result[i++] = x.item;
        }
        return result;
    }

    // ---------- внутрішні операції з вузлами ----------

    private void linkLast(Object e) {
        Node l = last;
        Node n = new Node(l, e, null);
        last = n;
        if (l == null) {
            first = n;
        } else {
            l.next = n;
        }
        size++;
    }

    private void linkBefore(Object e, Node succ) {
        Node pred = succ.prev;
        Node n = new Node(pred, e, succ);
        succ.prev = n;
        if (pred == null) {
            first = n;
        } else {
            pred.next = n;
        }
        size++;
    }

    private Object unlink(Node x) {
        Object item = x.item;
        Node prev = x.prev;
        Node next = x.next;

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
        return item;
    }

    // Йдемо з того кінця, який ближче - максимум size/2 кроків.
    private Node node(int index) {
        if (index < (size >> 1)) {
            Node x = first;
            for (int i = 0; i < index; i++) x = x.next;
            return x;
        } else {
            Node x = last;
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
        for (Node x = first; x != null; x = x.next) {
            sb.append(x.item);
            if (x.next != null) sb.append(", ");
        }
        return sb.append(']').toString();
    }
}
