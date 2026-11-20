package lab.collections;

import org.junit.Test;

import java.util.RandomAccess;

import static org.junit.Assert.assertTrue;

public class MyArrayListTest extends AbstractMyListTest {

    @Override
    protected <T> MyList<T> newList() {
        return new MyArrayList<>(2); // маленька ємність, щоб частіше перевіряти розширення
    }

    @Test
    public void implementsRandomAccess() {
        assertTrue(new MyArrayList<>() instanceof RandomAccess);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeCapacityThrows() {
        new MyArrayList<>(-1);
    }
}
