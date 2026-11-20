package lab.collections;

// Спрощений аналог java.util.List без generics , як в умові - елементи типу Object
public interface MyList {

    // додає елемент в кінець списку 
    void add(Object e);

    // додає елемент у вказане місце списку (index від 0 до size включно)
    void add(int index, Object element);

    // додає масив елементів в кінець списку
    void addAll(Object[] c);

    // додає масив елементів у вказане місце списку
    void addAll(int index, Object[] c);

    // повертає елемент за індексом
    Object get(int index);

    // видаляє елемент за індексом і повертає його
    Object remove(int index);

    // змінює значення елемента
    void set(int index, Object element);

    // індекс першого входження або -1
    int indexOf(Object o);

    // розмір списку
    int size();

    // копія вмісту у вигляді масиву
    Object[] toArray();
}
