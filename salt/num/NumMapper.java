package salt.num;

import java.util.Iterator;

/**
 * Map a iterator of numbers to another iterator of numbers.  Or iterables to iterables.
 * @author Seun
 */
abstract public class NumMapper<X extends Number, Y extends Number> {
    /**
     * Called for each element in the Inputstream.  A return value of null skips the current element.
     */
    abstract public  Y map(X element);

    /** Apply this transformation to each element of an iterable. */
    final public NumStream<Y> applyTo(final Iterator<X> iterator) {
        return new NumStream<Y>() {
            public Y produce() {
                while(iterator.hasNext()) {
                    final Y value = map(iterator.next());
                    if (value != null) return value;
                }
                return null;
            }
        };
    }

    final public NumSequence<Y> applyTo(final Iterable<X> iterable) {
        return new NumSequence<Y>() {
            public NumStream<Y> iterator() { return applyTo(iterable.iterator()); }
        };
    }
}