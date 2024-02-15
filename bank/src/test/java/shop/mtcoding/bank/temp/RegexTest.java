package shop.mtcoding.bank.temp;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class RegexTest {

    @Test
    public void 한글만가능_test() throws Exception {
        String value = "한글만허용확인";
        // 한글 전체 허용범위 지정, 나머지 정보가 입력되면 false 가 반환된다.
        boolean result = Pattern.matches("^[ㄱ-ㅎ가-힣]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 한글불가능_test() throws Exception {
        String value = "ㄱ";
        // 한글이 한글자라도 입력되면 false, 나머지 입력의 경우 true 가 반환된다.
        // 아무 입력도 들어오지 않았을 경우 또한 상정하기 위해 * 를 사용하였다.
        boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]*$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어만가능_test() throws Exception {
        String value = "ssar";
        // 영어만 입력되었을 경우 true, 그외 나머지가 입력될 경우 false 가 반환된다.
        boolean result = Pattern.matches("^[a-zA-Z]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어불가능_test() throws Exception {
        String value = "가22";
        // 영어가 한 글자라도 입력되었을 경우 false, 그외 나머지가 입력될 경우 false 가 반환된다.
        boolean result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어와숫자만가능_test() throws Exception {
        String value = "abc123";
        // 영어와 숫자입력이 들어왔을 때만 true, 그외 나머지가 입력될 경우 false 가 반환된다.
        boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);
        System.out.println("테스트 : " + result);
    }

    @Test
    public void 영어만가능하고_길이는최소2최대4_test() throws Exception {
        String value = "abcd";
        // 영어만 입력을 받되, 중괄호를 통해 최소, 최대 길이를 지정해주었다.
        // 이 조건이 만족될 경우 true, 그렇지 않을 경우 false 를 반환한다.
        boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);
        System.out.println("테스트 : " + result);
    }
}
