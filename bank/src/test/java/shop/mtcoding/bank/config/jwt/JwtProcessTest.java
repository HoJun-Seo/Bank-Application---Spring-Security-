package shop.mtcoding.bank.config.jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class JwtProcessTest {

    @Test
    public void create_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);

        // when
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println(" 테스트 : " + jwtToken);

        // then
        assertTrue(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
        // JwtVO.TOKEn_PREFIX == "Bearer"
    }

    @Test
    public void verify_test() throws Exception {
        // given
        // create_test() 에서 만들어진 JWT 토큰값에서 "Bearer" 를 제외한 부분 모두 가져오기
        String jwtToken = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYW5rIiwiZXhwIjoxNzEwMzEyNTIwLCJpZCI6MSwicm9sZSI6IkNVU1RPTUVSIn0.501YaQ8Xu5yqiaWXgq3GdnoFBPxvYvlEYc4bxIt5-HemwhjjTEaowWsy_Vy40601jDzUodz5K6ZEAWGIgD70WQ";

        // when
        LoginUser loginUser = JwtProcess.verify(jwtToken);
        // System.out.println(" 테스트 : " + loginUser.getUser().getId());

        // then
        assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
}
