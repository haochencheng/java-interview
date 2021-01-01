import org.junit.jupiter.api.Test;

import java.util.Random;

public class JunitTest {

    @Test
    public void randomTest(){
        Random random = new Random();
        int randomKey = random.nextInt(10);
        System.out.println(randomKey);
    }

}
