package salt.num;

import java.util.Iterator;
import salt.Stream;

/**
 * A numeric stream. Lets you manipulate the numbers in all sorts of interesting ways.
 * @author Seun
 */
abstract public class NumStream<E extends Number> extends Stream<E>{
    public static <E extends Number> NumStream<E> wrap (final Iterator<E> iterator) {
        if (iterator instanceof NumStream) return ((NumStream<E>)iterator); //don't rewrap streams
        else return new NumStream<E>() {
            public E produce() { return iterator.hasNext() ? iterator.next() : null; }
        };
    }

    //RANGE:
    public static NumStream<Integer> range(final int begin, final int end, final int step) {
        if (step > 0) return new NumStream<Integer>() {
            int current = begin;
            public Integer produce() {
                if (current < end) { final int v = current; current += step; return v; }
                else return null;
            }
        };
        else return new NumStream<Integer>() {
            int current = begin;
            public Integer produce() {
                if (current > end) { final int v = current; current += step; return v; }
                else return null;
            }
        };
    }
    public static NumStream<Integer> range(int begin, int end) { return range(begin, end, 1); }
    public static NumStream<Integer> range(int end) { return range(0, end, 1); }
    public static NumStream<Integer> range() { return range(0, Integer.MAX_VALUE, 1); }

    //SUM:
    public static Number sum(Iterator<? extends Number> iterator) {
        double sum = 0;
        while(iterator.hasNext()) sum += iterator.next().doubleValue();
        return sum;
    }
    public Number sum() { return sum((Iterator<? extends Number>)this); }

    //PRODUCT:
    public static Number product(Iterator<? extends Number> iterator) {
        double product = 0;
        while(iterator.hasNext()) product *= iterator.next().doubleValue();
        return product;
    }
    public Number product() { return product((Iterator<? extends Number>)this); }

    //AVERAGE:
    public static Number average(Iterator<? extends Number> iterator) {
        double sum = 0; int n = 0;
        while(iterator.hasNext()) { sum += iterator.next().doubleValue(); n += 1; }
        return sum / n;
    }
    public Number average() { return average((Iterator<? extends Number>)this); }


    //PLUS:
    public static NumStream<Double> plus(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new NumStream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() + second.next().doubleValue();
            }
        };
    }

    public NumStream<Double> plus(final Iterator<? extends Number> iterator) {
        return plus((Iterator<? extends Number>)this, iterator);
    }

    //MINUS:
    public static NumStream<Double> minus(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new NumStream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() - second.next().doubleValue();
            }
        };
    }

    public NumStream<Double> minus(final Iterator<? extends Number> iterator) {
        return minus((Iterator<? extends Number>)this, iterator);
    }

    //TIMES:
    public static NumStream<Double> times(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new NumStream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() * second.next().doubleValue();
            }
        };
    }

    public NumStream<Double> times(final Iterator<? extends Number> iterator) {
        return times(iterator, (Iterator<? extends Number>)this);
    }

    //OVER:
    public static NumStream<Double> over(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new NumStream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() / second.next().doubleValue();
            }
        };
    }

    public NumStream<Double> over(final Iterator<? extends Number> iterator) {
        return over(iterator, (Iterator<? extends Number>)this);
    }
}
