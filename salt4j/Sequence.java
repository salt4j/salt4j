package salt4j;

import java.util.AbstractCollection;
import java.util.Iterator;

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

    //RANGE:

    public static Sequence<Integer> range(final int begin, final int end, final int step) {
        return new Sequence<Integer>() {
            public Stream<Integer> iterator() { return Stream.range(begin, end, step);  }
        };
    }
    public static Sequence<Integer> range(int begin, int end) { return range(begin, end, 1); }
    public static Sequence<Integer> range(int end) { return range(0, end, 1); }
    public static Sequence<Integer> range() { return range(0, Integer.MAX_VALUE, 1); }

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

    //SUM:
    public static Number sum(Iterable<Number> it) { return Stream.sum(it.iterator()); }
    public Number sum() { return sum((Iterable<Number>)this); }

    //PRODUCT:
    public static Number product(Iterable<Number> it) {return Stream.product(it.iterator());}
    public Number product() { return product((Iterable<Number>)this); }

    //AVERAGE:
    public static Number average(Iterable<Number> it) {return Stream.average(it.iterator());}
    public Number average() { return average((Iterable<Number>)this); }

    //PLUS:
    public static Sequence<Double> plus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.plus(first.iterator(), second.iterator()); }
        };
    }

    public Sequence<Double> plus(final Iterable<? extends Number> other) {
        return plus((Iterable<Number>)this, other);
    }

    //MINUS:
    public static Sequence<Double> minus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.minus(first.iterator(), second.iterator()); }
        };
    }

    public Sequence<Double> minus(final Iterable<? extends Number> other) {
        return minus((Iterable<Number>)this, other);
    }

    //TIMES:
    public static Sequence<Double> times(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.times(first.iterator(), second.iterator()); }
        };
    }

    public Sequence<Double> times(final Iterable<? extends Number> other) {
        return times((Iterable<Number>)this, other);
    }

    //OVER:
    public static Sequence<Double> over(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.over(first.iterator(), second.iterator()); }
        };
    }

    public Sequence<Double> over(final Iterable<? extends Number> other) {
        return over((Iterable<Number>)this, other);
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