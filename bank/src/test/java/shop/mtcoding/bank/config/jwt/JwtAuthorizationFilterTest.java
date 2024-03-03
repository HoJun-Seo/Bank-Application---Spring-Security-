package shop.mtcoding.bank.config.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

// DB 쓰진 않을것이기 때문에 @Transactinal 은 필요치 않다.
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class JwtAuthorizationFilterTest {

    @Autowired
    private MockMvc mvc;

    // JwtAuthorizatoinFilter 의 doFilterInternal 메서드 성공 테스트

    // 인증이 필요한 URI 에 대해 JWT 토큰을 통해 인증을 받은 상태에서 요청을 보내는 경우
    @Test
    public void authorization_success_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : " + jwtToken);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/s/hello/test")
                .header(JwtVO.HEADER, jwtToken));
        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    // 인증이 되어있지 않은 상태에서 인증이 필요한 URI 에 대해 요청을 보내는 경우
    @Test
    public void authorization_fail_test() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/s/hello/test"));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isUnauthorized());
        // SecurityConfig 에서 인증과정에 오류가 발생할 경우 HttpStatus.UNAUTHORIZED 에러가 발생하도록 설정해두었다.
    }

    // admin 권한이 필요한 URI 에 대해 admin 권한을 가지고 요청을 보내는 경우
    @Test
    public void authorization_admin_success_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.ADMIN).build();
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : " + jwtToken);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/admin/hello/test")
                .header(JwtVO.HEADER, jwtToken));
        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    // admin 권한이 필요한 URI 에 대해 인증은 되어있으나, 권한이 없는 상태로 요청을 보내는 경우
    @Test
    public void authorization_admin_fail_customer_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : " + jwtToken);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/admin/hello/test")
                .header(JwtVO.HEADER, jwtToken));
        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isForbidden());
        // SecurityConfig 에서 권한이 없어 접근에 실패할 경우 HttpStatus.FORBIDDEN 에러가 발생하도록 설정해두었다.

    }

    // admin 권한이 필요한 URI 에 대해 인증이 되어있지 않은 상태로 요청을 보내는 경우
    @Test
    public void authorization_admin_fail_notAuthorize_test() throws Exception {
        // given

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/admin/hello/test"));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isUnauthorized());
        // SecurityConfig 에서 인증과정에 오류가 발생할 경우 HttpStatus.UNAUTHORIZED 에러가 발생하도록 설정해두었다.
    }
}
