package salt;

import java.util.AbstractCollection;
import java.util.Iterator;
import salt.util.NumStream;

/**
 * A collection of useful static methods that transform Iterators and Iterables lazily.
 * And an Iterable wrapper class that provides syntatic sugar for using the methods.
 * @author Seun Osewa
 */
abstract public class Sequence<E> extends AbstractCollection<E> {
    abstract public Stream<E> iterator();

    //WRAP: Create an Sequence from an existing collection or iterator.
    public static <E> Sequence<E> wrap(final Iterable<E> source) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.wrap(source.iterator()); }
        };
    }
    
    public static <E> Sequence<E> wrap(final E... array) {
        return new Sequence<E>() {
            public Stream<E> iterator() {
                return new Stream<E>() {
                    int count;
                    public E produce() {
                        if (count < array.length) { count++; return array[count-1]; } else return null;
                    }
                };
            }
        };
    }

    /** Create a sequence that contains just one item */
    public static <E> Sequence<E> wrapOne (final E item) { return repeat(item, 1); }

    // SIZE:
    public static <E> int size(Iterable<E> iterable) { return Stream.size(iterable.iterator()); }
    public int size() { return size(this); }

    //JOIN:
    public static String join(Iterable iterable, String delimiter) {
        return Stream.join(iterable.iterator(), delimiter);
    }
    public String join(String delimiter) { return join(this, delimiter); }

    /** Visual representation of the elements of the Iterable. */
    public String toString() { return "[" + join(", ") + "]"; }

    /** Returns a new Sequence that contains at most the first n elements of the iterable. */
    public static <E> Sequence<E> take(final Iterable<E> iterable, final int n) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.take(iterable.iterator(), n); }
        };
    }
    public Sequence<E> take(int n) { return take(this, n); }

    /** returns a new sequence that omits the first n elements of the iterable */
    public static <E> Sequence<E> skip(final Iterable<E> iterable, final int n) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.skip(iterable.iterator(), n); }
        };
    }
    /** Sequence.skip(this, n) */
    public Sequence<E> skip(int n) { return skip(this, n); }

    //ABOVE:
    public static <E extends Comparable<E>> Iterable <E> above (final Iterable<E> iterable, final E ref) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.above(iterable.iterator(), ref); }
        };
    }
    public Sequence<E> above(E ref) {
        return (Sequence<E>)Sequence.above((Iterable<Comparable>)this, (Comparable)ref);
    }

    //NOTABOVE:
    public static <E extends Comparable<E>> Iterable <E> notAbove (final Iterable<E> iterable, final E ref) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.notAbove(iterable.iterator(), ref); }
        };
    }
    public Sequence<E> notAbove(E ref) {
        return (Sequence<E>)Sequence.notAbove((Iterable<Comparable>)this, (Comparable)ref);
    }

    //BELOW:
    public static <E extends Comparable<E>> Iterable <E> below (final Iterable<E> iterable, final E ref) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.below(iterable.iterator(), ref); }
        };
    }
    public Sequence<E> below(E ref) {
        return (Sequence<E>)Sequence.below((Iterable<Comparable>)this, (Comparable)ref);
    }

    //NOTBELOW:
    public static <E extends Comparable<E>> Iterable <E> notBelow (final Iterable<E> iterable, final E ref ) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.notBelow(iterable.iterator(), ref); }
        };
    }
    public Sequence<E> notBelow(E ref) {
        return (Sequence<E>)Sequence.notBelow((Iterable<Comparable>)this, (Comparable)ref);
    }

    //ZIP:
    public static Sequence<Object[]> zip(final Iterable... iterables) {
        return new Sequence<Object[]>() {
            public Stream<Object[]> iterator() {
                Iterator<Object[]>[] iterators = new Iterator[iterables.length];
                for (int i = 0; i<iterables.length; i++) iterators[i]=iterables[i].iterator();
                return Stream.zip(iterators);
            }
        };
    }
    public Sequence<Object[]> zipWith(final Iterable... iterables) {
        return new Sequence<Object[]>() {
            public Stream<Object[]> iterator() {
                Iterator<Object[]>[] iterators = new Iterator[iterables.length + 1];
                iterators[0] = this.iterator();
                for (int i = 0; i<iterables.length; i++) iterators[i +(1)]=iterables[i].iterator();
                return Stream.zip(iterators);
            }
        };
    }


    //MAX:
    public static <E extends Comparable> E max(Iterable<E> it) {return Stream.max(it.iterator());}
    public<F extends Comparable> F max() { return max((Iterable<F>)this); }

    //MIN:
    public static <E extends Comparable> E min(Iterable<E> it) {return Stream.min(it.iterator());}
    public<F extends Comparable> F min() { return min((Iterable<F>)this); }

    //REPEAT:
    public static <E> Sequence<E> repeat(final E item, final int n) {
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.repeat(item, n); }
        };
    }
    public static <E> Sequence<E> repeat(final E item) { return repeat(item, Integer.MAX_VALUE); }

    //CONCAT:
    public static<E> Sequence<E> concat(Iterable<E>... iterables) {
        final Iterator<E>[] iterators = new Iterator[iterables.length];
        for(int i = 0; i<iterables.length;i++) iterators[i] = iterables[i].iterator();
        return new Sequence<E>() {
            public Stream<E> iterator() {
                return Stream.concat(iterators);
            }
        };
    }
    public Sequence<E> concatWith(Iterable<E>... iterables) {
        final Iterator<E>[] iterators = new Iterator[iterables.length+1];
        iterators[0] = this.iterator();
        for(int i = 0; i<iterables.length;i++) iterators[i+1] = iterables[i].iterator();
        return new Sequence<E>() {
            public Stream<E> iterator() { return Stream.concat(iterators); }
        };
    }
}