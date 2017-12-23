import org.junit.Test;

/**
 * Created by mjj on 2017/12/23
 */
public class TestA {

    @Test public void test(){
        int x = 2;
        int y = 1;

        System.out.println("a: " + ((x + 1) * 2 - y - 1));
        System.out.println("b: " + (x + (1 + y) * 6 + 2));
    }
}
