package salt4j;
import java.util.Iterator;
import static java.lang.System.out;
/**
 * Various tests. Not part of the official API.
 * @author Seun Osewa
 */
public class Main {
    public static void main(String[] args) {
        Sequence<Integer>s = Sequence.range();
        
        out.println(s.skip(10).times(5).take(10));
    }
}