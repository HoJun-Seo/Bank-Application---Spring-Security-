package shop.mtcoding.bank.temp;

import org.junit.jupiter.api.Test;

public class LongTypeTest {

    @Test
    public void longtype_test() {
        Long number1 = 1000L;
        Long number2 = 1000L;

        if (number1 == number2) {
            System.out.println("number1 과 number2 는 값이 동일하다.");
        } else {
            System.out.println("number1 과 number2 는 값이 동일하지 않다.");
        }
    }
}
