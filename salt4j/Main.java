package salt4j;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import salt4j.io.Manager;
import static java.lang.System.out;
/**
 * Various tests. Not part of the official API.
 * @author Seun Osewa
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Sequence<Integer>s = Sequence.range();
        
        //out.println(s.skip(10).times(5).take(10));

        InputStream in = new ByteArrayInputStream(new byte[]{9,1,0,9});

        new Manager.One<InputStream>() {
            public void run(InputStream x) throws IOException {
                System.out.println(x.read());
            }
        }.with(in);
    }
}