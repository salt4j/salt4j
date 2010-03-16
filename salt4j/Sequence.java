package salt4j;

import java.util.AbstractCollection;
import java.util.Iterator;
import salt4j.tuples.Tuple2;
import salt4j.tuples.Tuple3;

/**
 * A collection of useful static methods that transform Iterators and Iterables lazily.
 * And an Iterable wrapper class that provides syntatic sugar for using the methods.
 * @author Seun Osewa
 */
abstract public class Sequence<E> extends AbstractCollection<E> {
    abstract public Stream<E> iterator();

    //WRAP: Create an Sequence from an existing collection or iterator.
    public static <E> Sequence<E> wrap(final Iterable<E> source) {
        if (source instanceof Sequence) return (Sequence<E>)source;
        else return new Sequence<E>() {
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

    //ZIP2:
    public static <A, B> Sequence<Tuple2<A,B>> zip2(final Iterable<A> ita, final Iterable<B> itb) {
        return new Sequence<Tuple2<A, B>>() {
            public Stream<Tuple2<A, B>> iterator() { return Stream.zip2(ita.iterator(), itb.iterator()); }
        };
    }
    public <F> Sequence<Tuple2<E, F>> zip2(final Iterable<F> other) { return zip2(this, other); }

    //ZIP3:
    public static <A, B, C> Sequence<Tuple3<A, B, C>> zip3(final Iterable<A> ita, final Iterable<B> itb,
            final Iterable<C> itc) {
        return new Sequence<Tuple3<A, B, C>>() {
            public Stream<Tuple3<A, B, C>> iterator() {
                return Stream.zip3(ita.iterator(), itb.iterator(), itc.iterator());
            }
        };
    }
    public <F, G> Sequence<Tuple3<E, F, G>> zip3(Iterable<F> f, Iterable<G> g) { return zip3(this, f, g); }

    //SUM:
    public static Number sum(Iterable<? extends Number> it) { return Stream.sum(it.iterator()); }
    public Number sum() { return sum((Iterable<? extends Number>)this); }

    //PRODUCT:
    public static Number product(Iterable<? extends Number> it) {return Stream.product(it.iterator());}
    public Number product() { return product((Iterable<? extends Number>)this); }

    //AVERAGE:
    public static Number average(Iterable<? extends Number> it) {return Stream.average(it.iterator());}
    public Number average() { return average((Iterable<? extends Number>)this); }

    //PLUS:
    public static Sequence<Double> plus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.plus(first.iterator(), second.iterator()); }
        };
    }
    public Sequence<Double> plus(final Iterable<? extends Number> other) {
        return plus((Iterable<? extends Number>)this, other);
    }
    public Sequence<Double> plus(Number value) { return plus(repeat(value)); }

    //MINUS:
    public static Sequence<Double> minus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.minus(first.iterator(), second.iterator()); }
        };
    }
    public Sequence<Double> minus(final Iterable<? extends Number> other) {
        return minus((Iterable<? extends Number>)this, other);
    }
    public Sequence<Double> minus(Number value) { return minus(repeat(value)); }

    //TIMES:
    public static Sequence<Double> times(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.times(first.iterator(), second.iterator()); }
        };
    }
    public Sequence<Double> times(final Iterable<? extends Number> other) {
        return times((Iterable<? extends Number>)this, other);
    }
    public Sequence<Double> times(Number value) { return times(repeat(value)); }

    //OVER:
    public static Sequence<Double> over(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new Sequence<Double>() {
            public Stream<Double> iterator() { return Stream.over(first.iterator(), second.iterator()); }
        };
    }
    public Sequence<Double> over(final Iterable<? extends Number> other) {
        return over((Iterable<? extends Number>)this, other);
    }
    public Sequence<Double> over(Number value) { return over(repeat(value)); }

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
    public static<E> Sequence<E> concat(final Iterable<E>... iterables) {
        return new Sequence<E>() {
            public Stream<E> iterator() {
                final Iterator<E>[] iterators = new Iterator[iterables.length];
                for(int i = 0; i<iterables.length;i++) iterators[i] = iterables[i].iterator();
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