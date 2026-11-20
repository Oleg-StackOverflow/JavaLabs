package lab.collections;

import org.junit.Test;

import java.util.RandomAccess;

import static org.junit.Assert.assertFalse;

public class MyLinkedListTest extends AbstractMyListTest {

    @Override
    protected MyList newList() {
        return new MyLinkedList();
    }

    @Test
    public void doesNotImplementRandomAccess() {
        assertFalse(new MyLinkedList() instanceof RandomAccess);
    }
}
