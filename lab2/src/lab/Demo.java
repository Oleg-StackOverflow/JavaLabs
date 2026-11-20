package lab;

import lab.cache.LruCache;
import lab.collections.MyArrayList;
import lab.collections.MyLinkedHashSet;
import lab.collections.MyLinkedList;
import lab.collections.MyList;

import java.util.RandomAccess;

//Демонстрація всіх трьох частин завдання.
public class Demo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. MyList ===");
        demoList(new MyArrayList());
        demoList(new MyLinkedList());

        System.out.println("\n=== 2. MyLinkedHashSet ===");
        MyLinkedHashSet<String> set = new MyLinkedHashSet<>();
        for (String s : "banana apple cherry apple banana date".split(" ")) {
            set.add(s);
        }
        System.out.println("додали: banana apple cherry apple banana date");
        System.out.println("set = " + set + ", size = " + set.size());
        set.remove("apple");
        System.out.println("після remove(apple): " + set);

        System.out.println("\n=== 3. Cache (maxSize = 3, ttl = 500 мс) ===");
        LruCache<String, String> cache = new LruCache<>(3, 500, System::currentTimeMillis,
                (k, v, cause) -> System.out.println("   [listener] " + k + " -> " + cause));
        cache.put("ua", "Київ");
        cache.put("pl", "Варшава");
        cache.put("de", "Берлін");
        cache.get("ua");
        System.out.println("get(ua), потім put(fr) - має витіснитись pl:");
        cache.put("fr", "Париж");
        System.out.println("   " + cache);
        try {
            cache.put("it", null);
        } catch (NullPointerException e) {
            System.out.println("put(it, null) -> NullPointerException: " + e.getMessage());
        }
        System.out.println("чекаємо 600 мс...");
        Thread.sleep(600);
        System.out.println("get(ua) = " + cache.get("ua"));
        System.out.println("size = " + cache.size());
        System.out.println("stats: " + cache.stats());
    }

    private static void demoList(MyList list) {
        String name = list.getClass().getSimpleName();
        list.add("A");
        list.add("C");
        list.add(1, "B");
        list.addAll(new Object[]{"E", "F"});
        list.addAll(3, new Object[]{"D"});
        System.out.println(name + ": " + list + ", size = " + list.size()
                + ", RandomAccess = " + (list instanceof RandomAccess));
        list.set(0, "a");
        Object removed = list.remove(5);
        System.out.println("   set(0,a), remove(5)=" + removed + " -> " + list
                + ", indexOf(D) = " + list.indexOf("D"));
    }
}
