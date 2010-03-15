package salt.num;

import salt.Sequence;

/**
 * A numeric sequence that can be reproduced.
 * @author Seun Osewa
 */
abstract public class NumSequence<E extends Number> extends Sequence<E> {
    abstract public NumStream iterator();

    //WRAP: Create an Sequence from an existing collection or iterator.
    public static <E extends Number> NumSequence<E> wrap(final Iterable<E> source) {
        return new NumSequence<E>() {
            public NumStream<E> iterator() { return NumStream.wrap(source.iterator()); }
        };
    }

    public static <E extends Number> NumSequence<E> wrap(final E... array) {
        return new NumSequence<E>() {
            public NumStream<E> iterator() {
                return new NumStream<E>() {
                    int count;
                    public E produce() {
                        if (count < array.length) { count++; return array[count-1]; } else return null;
                    }
                };
            }
        };
    }

    //RANGE:
    public static NumSequence<Integer> range(final int begin, final int end, final int step) {
        return new NumSequence<Integer>() {
            public NumStream<Integer> iterator() { return NumStream.range(begin, end, step);  }
        };
    }
    public static NumSequence<Integer> range(int begin, int end) { return range(begin, end, 1); }
    public static NumSequence<Integer> range(int end) { return range(0, end, 1); }
    public static NumSequence<Integer> range() { return range(0, Integer.MAX_VALUE, 1); }
    //SUM:
    public static Number sum(Iterable<Number> it) { return NumStream.sum(it.iterator()); }
    public Number sum() { return sum((Iterable<Number>)this); }

    //PRODUCT:
    public static Number product(Iterable<Number> it) {return NumStream.product(it.iterator());}
    public Number product() { return product((Iterable<Number>)this); }

    //AVERAGE:
    public static Number average(Iterable<Number> it) {return NumStream.average(it.iterator());}
    public Number average() { return average((Iterable<Number>)this); }

    //PLUS:
    public static NumSequence<Double> plus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new NumSequence<Double>() {
            public NumStream<Double> iterator() { return NumStream.plus(first.iterator(), second.iterator()); }
        };
    }

    public NumSequence<Double> plus(final Iterable<? extends Number> other) {
        return plus((Iterable<Number>)this, other);
    }

    //MINUS:
    public static NumSequence<Double> minus(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new NumSequence<Double>() {
            public NumStream<Double> iterator() { return NumStream.minus(first.iterator(), second.iterator()); }
        };
    }

    public NumSequence<Double> minus(final Iterable<? extends Number> other) {
        return minus((Iterable<Number>)this, other);
    }

    //TIMES:
    public static NumSequence<Double> times(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new NumSequence<Double>() {
            public NumStream<Double> iterator() { return NumStream.times(first.iterator(), second.iterator()); }
        };
    }

    public NumSequence<Double> times(final Iterable<? extends Number> other) {
        return times((Iterable<? extends Number>)this, other);
    }

    //OVER:
    public static NumSequence<Double> over(final Iterable<? extends Number> first,
            final Iterable<? extends Number> second) {
        return new NumSequence<Double>() {
            public NumStream<Double> iterator() { return NumStream.over(first.iterator(), second.iterator()); }
        };
    }

    public NumSequence<Double> over(final Iterable<? extends Number> other) {
        return over((Iterable<Number>)this, other);
    }
}