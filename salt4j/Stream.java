package salt4j;

import java.util.Iterator;
import java.util.NoSuchElementException;
import salt4j.tuples.Tuple2;
import salt4j.tuples.Tuple3;

/**
 * A helper class to make creating read-only iterators easy. You only have to implement
 * one method: produce(). Every Stream is an 100% compatible java Iterator.
 * @author Seun Osewa
 */
abstract public class Stream<E> implements Iterator<E> {
    /**
     *  Produce the next element of the Stream. This is the only method you need to implement.
     *  The Stream is terminated when this function returns null.
     */
    abstract public E produce();
    
    //THE FOLOWING FUNCTIONS IMPLEMENT THE ITERATOR INTERFACE:
    private E buffer = null;

    final public boolean hasNext() {
        if (buffer != null) return true;
        else { buffer = produce(); return buffer != null; }
    }

    final public E next() {
        if ((buffer != null) || hasNext()) { final E r = buffer; buffer = null; return r; }
        else throw new NoSuchElementException();
    }

    /** Exhaust the iterator and return the last value. Empty iterator returns null*/
    final public E lastValue() {
        //E buf = null; while(hasNext()) buf = next(); return buf;
        E localBuf = null;
        for(;;) {
            final E buf = produce();
            if (buf != null) localBuf = buf; else return localBuf;
        }
    }

    /**
     *  To implement remove() correctly, check whether buffered is true.
     *  if it is, true an exception or remove the former element if possible.
     */
    public void remove() { throw new UnsupportedOperationException(); }

    /**
     *  Wrap an Iterator in a Stream to take advantage of syntactic sugar.
     *  Avoids double-wrapping streams.
     */
    public static <E> Stream<E> wrap (final Iterator<E> iterator) {
        if (iterator instanceof Stream) return ((Stream<E>)iterator); //don't rewrap streams
        else return new Stream<E>() {
            public E produce() { return iterator.hasNext() ? iterator.next() : null; }
        };
    }

    public static <E> Stream<E> wrapOne (final E item) { return repeat(item, 1); }

    //SIZE:
    public static <E> int size(Iterator<E> iterator) {
        int count = 0;
        while (iterator.hasNext()) { iterator.next(); count ++; }
        return count;
    }
    public int size() { return size(this); }

    //ISEMPTY
    public boolean isEmpty() { return !this.hasNext(); }
    
    //RANGE:
    public static Stream<Integer> range(final int begin, final int end, final int step) {
        if (step > 0) return new Stream<Integer>() {
            int current = begin;
            public Integer produce() {
                if (current < end) { final int v = current; current += step; return v; }
                else return null;
            }
        };
        else return new Stream<Integer>() {
            int current = begin;
            public Integer produce() {
                if (current > end) { final int v = current; current += step; return v; }
                else return null;
            }
        };
    }
    public static Stream<Integer> range(int begin, int end) { return range(begin, end, 1); }
    public static Stream<Integer> range(int end) { return range(0, end, 1); }
    public static Stream<Integer> range() { return range(0, Integer.MAX_VALUE, 1); }

    //JOIN:
    public static String join(Iterator iterator, String delimiter) {
        if (iterator.hasNext()) {
            StringBuilder builder = new StringBuilder(iterator.next().toString());
            while (iterator.hasNext()) builder.append(delimiter).append(iterator.next());
            return builder.toString();
        } else return "";
    }
    public String join(String delimiter) { return join(this, delimiter); }

    //TOSTRING: uses join
    public String toString() { return "[" + join(", ") + "]"; }

    //TAKE:
    public static <E> Stream<E> take(final Iterator<E> iterator, final int n) {
        return new Stream<E>() {
            int count = 0;
            public E produce() {
                if (count < n && iterator.hasNext()) { count++; return iterator.next(); }
                else return null;
            }
        };
    }
    public Stream<E> take(int n) { return take(this, n); }

    public static <E> Stream<E> skip(final Iterator<E> iterator, final int n) {
        for(int i = 0; i < n; i++) if (iterator.hasNext()) iterator.next();
        return wrap(iterator);
    }
    public Stream<E> skip(int n) { return skip(this, n); }

    //SKIPPED: also lazy. skip happens when you read the resulting iterator
    public static <E> Stream<E> skipped(final Iterator<E> iterator, final int n) {
        return new Stream<E>() {
            boolean skipped = false;
            public E produce() {
                if (!skipped) {
                    for(int i = 0; i < n; i++) if (iterator.hasNext()) iterator.next();
                    skipped = true;
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }
    public Stream<E> skipped(int n) { return skipped(this, n); }

    //ABOVE:
    public static <E extends Comparable<E>> Stream <E> above (final Iterator<E> iterator, final E ref) {
        return new Stream<E>() {
            public E produce() {
                while(iterator.hasNext()) {
                    final E next = iterator.next();
                    if (next.compareTo(ref) > 0) return next;
                }
                return null;
            }
        };
    }
    public Stream<E> above(E ref) {
        return (Stream<E>)Stream.above((Iterator<Comparable>)this, (Comparable)ref);
    }

    //NOTABOVE:
    public static <E extends Comparable<E>> Stream <E> notAbove (final Iterator<E> iterator, final E reference) {
        return new Stream<E>() {
            public E produce() {
                while(iterator.hasNext()) {
                    final E next = iterator.next();
                    if (next.compareTo(reference) <= 0) return next;
                }
                return null;
            }
        };
    }
    public Stream<E> notAbove(E ref) {
        return (Stream<E>)Stream.notAbove((Iterator<Comparable>)this, (Comparable)ref);
    }

    //BELOW:
    public static <E extends Comparable<E>> Stream <E> below (final Iterator<E> iterator, final E ref) {
        return new Stream<E>() {
            public E produce() {
                while(iterator.hasNext()) {
                    final E next = iterator.next();
                    if (next.compareTo(ref) < 0) return next;
                }
                return null;
            }
        };
    }
    public Stream<E> below(E ref) {
        return (Stream<E>)Stream.below((Iterator<Comparable>)this, (Comparable)ref);
    }

    //NOTBELOW:
    public static <E extends Comparable<E>> Stream <E> notBelow (final Iterator<E> iterator, final E ref) {
        return new Stream<E>() {
            public E produce() {
                while(iterator.hasNext()) {
                    final E next = iterator.next();
                    if (next.compareTo(ref) >= 0) return next;
                }
                return null;
            }
        };
    }
    public Stream<E> notBelow(E ref) {
        return (Stream<E>)Stream.notBelow((Iterator<Comparable>)this, (Comparable)ref);
    }

    //ZIP:
    public static Stream<Object[]> zip(final Iterator... iterators) {
        return new Stream<Object[]>() {
            public Object[] produce() {
                for (int i = 0; i<iterators.length; i++) if (!iterators[i].hasNext()) return null;
                Object[] row = new Object[iterators.length];
                for (int i = 0; i<iterators.length; i++) row[i] = iterators[i].next();
                return row;
            }
        };
    }

    //ZIP2:
    public static <A, B> Stream<Tuple2<A,B>> zip2(final Iterator<A> ita, final Iterator<B> itb) {
        return new Stream<Tuple2<A, B>>() {
            public Tuple2<A, B> produce() {
                if (ita.hasNext() && itb.hasNext()) {
                    return new Tuple2<A, B>(ita.next(), itb.next());
                } else return null;
            }
        };
    }
    public<F> Stream<Tuple2<E,F>> zip2(final Iterator<F> other) { return zip2(this, other); }

    //ZIP3:
    public static <A, B, C> Stream<Tuple3<A, B, C>> zip3(final Iterator<A> ita, final Iterator<B> itb,
            final Iterator<C> iterc) {
        return new Stream<Tuple3<A, B, C>>() {
            public Tuple3<A, B, C> produce() {
                if (ita.hasNext() && itb.hasNext() && iterc.hasNext()) {
                    return new Tuple3<A, B, C>(ita.next(), itb.next(), iterc.next());
                } else return null;
            }
        };
    }
    public<F,G> Stream<Tuple3<E,F,G>> zip3(final Iterator<F> otherf, Iterator<G> otherg) {
        return zip3(this, otherf, otherg);
    }

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
    public static Stream<Double> plus(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new Stream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() + second.next().doubleValue();
            }
        };
    }
    public Stream<Double> plus(final Iterator<? extends Number> iterator) {
        return Stream.plus((Iterator<? extends Number>)this, iterator);
    }
    public Stream<Double> plus(Number value) { return plus(repeat(value)); }
    
    //MINUS:
    public static Stream<Double> minus(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new Stream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() - second.next().doubleValue();
            }
        };
    }
    public Stream<Double> minus(final Iterator<? extends Number> iterator) {
        return Stream.minus((Iterator<? extends Number>)this, iterator);
    }
    public Stream<Double> minus(Number value) { return minus(repeat(value)); }

    //TIMES:
    public static Stream<Double> times(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new Stream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() * second.next().doubleValue();
            }
        };
    }
    public Stream<Double> times(final Iterator<? extends Number> iterator) {
        return Stream.times(iterator, (Iterator<? extends Number>)this);
    }
    public Stream<Double> times(Number value) { return times(repeat(value)); }



    //OVER:
    public static Stream<Double> over(final Iterator<? extends Number> first,
            final Iterator<? extends Number> second) {
        return new Stream<Double>() {
            public Double produce() {
                if (!(first.hasNext() && second.hasNext())) return null;
                return first.next().doubleValue() / second.next().doubleValue();
            }
        };
    }

    public Stream<Double> over(final Iterator<? extends Number> iterator) {
        return Stream.over(iterator, (Iterator<? extends Number>)this);
    }
    public Stream<Double> over(Number value) { return over(repeat(value)); }

    //MAX:
    public static <E extends Comparable> E max(Iterator<E> iterator) {
        if (iterator.hasNext()) {
            E max = iterator.next();
            while(iterator.hasNext()) {
                E next = iterator.next();
                if (next.compareTo(max) > 0) max = next;
            }
            return max;
        } else return null;
    }
    public<F extends Comparable> E max() { return (E)max((Iterator<F>)this); }

    //MIN:
    public static <E extends Comparable> E min(Iterator<E> iterator) {
        if (iterator.hasNext()) {
            E min = iterator.next();
            while(iterator.hasNext()) {
                E next = iterator.next();
                if (next.compareTo(min) < 0) min = next;
            }
            return min;
        } else return null;
    }
    public<F extends Comparable> E min() { return (E)min((Iterator<F>)this); }

    //REPEAT:
    public static <E> Stream<E> repeat(final E item, final int num) {
        return new Stream<E>() {
            int count = 0;
            public E produce() {
                if (count < num) { count ++; return item; } else return null;
            }
        };
    }
    public static <E> Stream<E> repeat(final E item) { return repeat(item, Integer.MAX_VALUE); }

    //CONCAT:
    public static<E> Stream<E> concat(final Iterator<E>... iterators) {
        return new Stream<E>() {
            Iterator<E> iterator = null; int count = 0;
            public E produce() {
                for(;;) {
                    if (iterator != null && iterator.hasNext()) return iterator.next();
                    else if (count < iterators.length) { iterator = iterators[count]; count++; }
                    else return null;
                }
            }
        };
    }

    public static<E> Sequence<E> toSequence(final Iterator<E> iterator) {
        class Node {
            final E[] items = (E[])(new Object[32]);
            int dataPos = 0;
            Node nextNode = null;

            E nextItem(int queryPos) {
                if (queryPos < dataPos) return items[queryPos];
                else if(dataPos < items.length && iterator.hasNext()) {
                    assert(queryPos == dataPos); //else we have a serious problem.
                    items[dataPos] = iterator.next();
                    dataPos ++;
                    return items[queryPos];
                } else return null;
            }

            Node nextNode() {
                if (nextNode != null) return nextNode; //reiteration
                else if (iterator.hasNext()) { nextNode = new Node(); return nextNode; }
                else return null;                
            }
        }

        class TeeStream extends Stream<E> {
            Node node; int pos = 0;
            public TeeStream(Node node) { this.node = node; }
            public E produce() {
                for(;;) {
                    if (node == null) return null;
                    E nextItem = node.nextItem(pos);
                    if ( nextItem != null) { pos += 1; return nextItem; }
                    node = node.nextNode(); pos = 0;
                }
            }
        }

        class TeeSequence extends Sequence<E> {
            Node firstNode;
            public TeeSequence(Node firstNode) { this.firstNode = firstNode; }
            public Stream<E> iterator() { return new TeeStream(firstNode); }
        }

        if (!iterator.hasNext()) {
            return Sequence.repeat(null, 0);
        } else return new TeeSequence(new Node());
    }
    public Sequence<E> toSequence() { return toSequence(this); }
}