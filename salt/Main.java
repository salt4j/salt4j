package salt;
import salt.num.NumSequence;
import static java.lang.System.out;
/**
 * Various tests. Not part of the official API.
 * @author Seun Osewa
 */
public class Main {
    public static void main(String[] args) {
        NumSequence<Integer>s = salt.num.NumSequence.range();
        out.println(s.times(Sequence.repeat(10)).take(10));
    }
}