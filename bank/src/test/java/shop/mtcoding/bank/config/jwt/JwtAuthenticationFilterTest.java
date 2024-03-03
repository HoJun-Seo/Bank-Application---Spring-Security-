package shop.mtcoding.bank.config.jwt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.user.UserReqDto.LoginReqDto;

@Transactional // 각 테스트 메서드가 종료될 때마다 데이터베이스가 롤백된다.
@ActiveProfiles("test") // application-test.yml 설정 적용
// 가짜환경(Mock) 으로 @SpringBoot 위에서 동작시켜야만 @Autowired 어노테이션 활용이 가능하다.
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc // MockMvc 의 의존성 주입을 위한 어노테이션
public class JwtAuthenticationFilterTest extends DummyObject {

    @Autowired
    private ObjectMapper om;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;

    // 유저 정보를 미리 삽입해야함
    @BeforeEach
    public void setUp() throws Exception {
        userRepository.save(newUser("ssar", "쌀"));
    }

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
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtVO.HEADER); // 헤더에 있는 JWT 토큰
                                                                                           // 추출(JwtVO.HEADER ==
                                                                                           // "Authorization")
        System.out.println("테스트 : " + jwtToken);
        System.out.println("테스트 : " + responseBody);

        // then
        // 결과 검증
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); // HTTPStatus 가 OK 여야 한다.
        assertNotNull(jwtToken); // JWT 토큰이 null 이 아니어야 한다.
        assertTrue(jwtToken.startsWith(JwtVO.TOKEN_PREFIX)); // JWT 토큰 문자열의 시작이 "Bearer" 이어야 한다.
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("ssar"));
        // 결과로 얻은 json 데이터의 data.username 값이 "ssar" 이어야 한다.
    }

    @Test
    public void unsuccessfulAuthentication_test() throws Exception {
        // given
        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("ssar");
        loginReqDto.setPassword("12345"); // 비밀번호 틀림
        String requestBody = om.writeValueAsString(loginReqDto);
        System.out.println("테스트 : " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtVO.HEADER);
        System.out.println("테스트 : " + jwtToken);
        System.out.println("테스트 : " + responseBody);

        // then
        // 로그인이 실패하면 HttpStatus 가 UNAUTHORIZED 이여야 한다.
        resultActions.andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
