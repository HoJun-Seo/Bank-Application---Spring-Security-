package shop.mtcoding.bank.config.jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class JwtProcessTest {

    // JWT 토큰 생성 및 반환 메서드
    private String createToken(User user) {
        return JwtProcess.create(new LoginUser(user));
    }

    @Test
    public void create_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();

        // when
        String jwtToken = createToken(user);
        System.out.println(" 테스트 : " + jwtToken);

        // then
        assertTrue(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
        // JwtVO.TOKEn_PREFIX == "Bearer"
    }

    @Test
    public void verify_test() throws Exception {
        // given
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();

        // when
        // Jwt 생성, 반환 및, "Bearer " 문자열 제거
        String jwtToken = createToken(user).replace(JwtVO.TOKEN_PREFIX, "");
        LoginUser loginUser = JwtProcess.verify(jwtToken);

        // then
        assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
}
