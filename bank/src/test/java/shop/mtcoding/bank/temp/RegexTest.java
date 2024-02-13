package shop.mtcoding.bank.temp;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class RegexTest {

    @Test
    public void 한글만가능_test() throws Exception {
        String value = "한글만허용확인";
        // 한글 전체 허용범위 지정, 나머지 정보가 입력되면 false 가 반환된다.
        boolean result = Pattern.matches("^[가-힣]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 한글불가능_test() throws Exception {

    }

    @Test
    public void 영어만가능_test() throws Exception {

    }

    @Test
    public void 영어불가능_test() throws Exception {

    }

    @Test
    public void 영어와숫자만가능_test() throws Exception {

    }

    @Test
    public void 영어만가능하고_길이는최소2최대4_test() throws Exception {

    }
}
