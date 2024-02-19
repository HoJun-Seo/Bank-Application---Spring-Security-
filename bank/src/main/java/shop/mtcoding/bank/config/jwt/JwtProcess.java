package shop.mtcoding.bank.config.jwt;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class JwtProcess {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // JWT 토큰 생성 메서드
    public static String create(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("bank") // 토큰의 제목
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtVO.EXPIRATION_TIME)) // 현재시간 + 일주일
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name()) // getRole() 에서 끝나면 이때 UserEnum 타입을 리턴받게 되는데
                                                                         // 여기에선 String 을 리턴 받아야 한다.
                .sign(Algorithm.HMAC512(JwtVO.SECRET));

        return JwtVO.TOKEN_PREFIX + jwtToken;
    }

    // JWT 토큰 검증 메서드 (return 되는 LoginUser 객체를 강제로 시큐리티의 세션에 직접 주입 - 강제 로그인)
    public static LoginUser verify(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(user);

        return loginUser;
    }
}
