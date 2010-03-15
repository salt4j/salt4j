package salt4j;

import java.util.Collection;

import static java.lang.System.out;

/**
 * Various tests. Not part of the official API.
 * @author Seun Osewa
 */
public class Main {
    public static void main4(String[] args) {
        Mapper<Integer, Integer> evenSquares = new Mapper<Integer, Integer>() {
            public Integer map(Integer x) { if (x % 2 == 0) return (x * x); else return null; }
        };
        out.println(Sequence.wrap(1, 2, 3, 4, 5, 6, 7.5, "duck").take(7).sum());
    }
    public static void main2(String[] args) {
        Stream<Integer> s = Stream.range(5);
        while(s.hasNext()) out.println(s.next());
    }
    public static void main(String[] args) {
        Stream<Integer> s = Stream.range(50000000);
        
        Stream<Float> s2 = new Stream<Float>() {
            int i = 0;
            public Float produce() {
                if(i < 50000000) {i+= 1; return new Float(i); } else return null;
            }
        };

        long begin = System.currentTimeMillis();
        //long sum = 0; while(s.hasNext()) sum += s.next();
        long sum = s.sum().longValue();
        //long sum=0; for(Integer i; null!=(i=s.produce());) sum += i;
        //long sum = 0; for(int i=0; i<50000000;i++) sum += i;
        //long sum = new Mapper<Integer, Long>() {
        //    long sum = 0L; public Long map(Integer element) { sum += element; return sum; }
        //}.applyTo(s).lastVal();
        long end = System.currentTimeMillis();
        out.println(sum + " " + (end-begin)/1000.0);
    }

    final public static Mapper<String, String> CAPITALIZER = new Mapper<String, String>() {
        public String map(String string) { return string.toUpperCase(); }
    };

    public Collection<String> capitalize(Collection<String> strings) {
        return new Mapper<String, String>() {
            public String map(String string) { return string.toUpperCase(); }
        }.applyTo(strings).iterator().toSequence();
    }
}