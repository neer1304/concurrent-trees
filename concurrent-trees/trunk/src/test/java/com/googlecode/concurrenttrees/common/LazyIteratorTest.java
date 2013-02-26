package com.googlecode.concurrenttrees.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Niall Gallagher
 */
public class LazyIteratorTest {

    static LazyIterator<Integer> newLazyIterator(final Integer... values) {
        return new LazyIterator<Integer>() {

            final Iterator<Integer> backingIterator = Arrays.asList(values).iterator();

            @Override
            protected Integer computeNext() {
                if (backingIterator.hasNext()) {
                    return backingIterator.next();
                }
                else {
                    return endOfData();
                }
            }
        };
    }

    static LazyIterator<Integer> newFaultyLazyIterator() {
        return new LazyIterator<Integer>() {
            @Override
            protected Integer computeNext() {
                throw new RuntimeException();
            }
        };
    }

    static void advance(LazyIterator<?> lazyIterator, int elementsToAdvance) {
        for (int i = 0; i < elementsToAdvance; i++) {
            lazyIterator.next();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        LazyIterator<Integer> lazyIterator = newLazyIterator(1, 2, 3, 4);
        lazyIterator.remove();
    }

    @Test
    public void testIteration() {
        LazyIterator<Integer> lazyIterator = newLazyIterator(1, 2, 3, 4);
        advance(lazyIterator, 3);
        Assert.assertTrue("should return true indefinitely until objects consumed", lazyIterator.hasNext());
        Assert.assertTrue("should return true indefinitely until objects consumed", lazyIterator.hasNext());

        List<Integer> values = new ArrayList<Integer>();
        while (lazyIterator.hasNext()) {
            values.add(lazyIterator.next());
        }
        Assert.assertEquals("[4]", values.toString());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNext_NoSuchElement() {
        LazyIterator<Integer> lazyIterator = newLazyIterator(1, 2, 3, 4);
        advance(lazyIterator, 4);
        lazyIterator.next();
    }

    @Test
    public void testHasNext_IllegalState() {
        LazyIterator<Integer> lazyIterator = newFaultyLazyIterator();
        try {
            lazyIterator.hasNext();
        }
        catch (RuntimeException expected) { }

        try {
            lazyIterator.hasNext();
            Assert.fail("should throw IllegalStateException on second call to hasNext, if previous call failed");
        }
        catch (IllegalStateException expected) { }
    }
}
