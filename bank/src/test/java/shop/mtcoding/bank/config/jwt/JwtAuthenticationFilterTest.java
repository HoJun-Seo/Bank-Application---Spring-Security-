package shop.mtcoding.bank.config.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;

import shop.mtcoding.bank.dto.user.UserReqDto.LoginReqDto;

// 가짜환경(Mock) 으로 @SpringBoot 위에서 동작시켜야만 @Autowired 어노테이션 활용이 가능하다.
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class JwtAuthenticationFilterTest {

    @Autowired
    private ObjectMapper om;

    @Test
    public void successfulAuthentication_test() throws Exception {
        // given
        // 본 코드에서는 생성자가 필요없기 때문에 굳이 LoginReqDto 객체에 대해
        // 생성자 코드를 만들어줄 필요가 없다.
        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("ssar");
        loginReqDto.setPassword("1234");
        String requestBody = om.writeValueAsString(loginReqDto);
        System.out.println("테스트 : " + requestBody);
        // when

        // then
    }

    @Test
    public void unsuccessfulAuthentication_test() throws Exception {
        // given

        // when

        // then
    }
}
