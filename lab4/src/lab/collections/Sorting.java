package lab.collections;

import java.util.Comparator;

// Сортування злиттям для масиву - спільне для обох списків. Стабільне, O(n log n)
final class Sorting {

    private Sorting() {
    }

    @SuppressWarnings("unchecked")
    static <E> void mergeSort(Object[] a, int n, Comparator<? super E> c) {
        Object[] tmp = new Object[n];
        for (int width = 1; width < n; width *= 2) {
            for (int lo = 0; lo < n - width; lo += 2 * width) {
                int mid = lo + width;
                int hi = Math.min(lo + 2 * width, n);
                int i = lo, j = mid, k = lo;
                while (i < mid && j < hi) {
                    // <= щоб рівні елементи не мінялися місцями (стабільність)
                    if (c.compare((E) a[i], (E) a[j]) <= 0) tmp[k++] = a[i++];
                    else tmp[k++] = a[j++];
                }
                while (i < mid) tmp[k++] = a[i++];
                while (j < hi) tmp[k++] = a[j++];
                System.arraycopy(tmp, lo, a, lo, hi - lo);
            }
        }
    }
}
