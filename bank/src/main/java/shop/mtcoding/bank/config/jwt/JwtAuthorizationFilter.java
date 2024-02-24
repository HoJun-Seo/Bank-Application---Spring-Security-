package shop.mtcoding.bank.config.jwt;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.mtcoding.bank.config.auth.LoginUser;

/*
 * 모든 주소에서 동작한다. (토큰 검증 필터)
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 토큰이 존재할 경우
        if (isHeaderVerify(request, response)) {
            // 토큰 앞쪽의 Bearer 문자열 제거
            String token = request.getHeader(JwtVO.HEADER).replace(JwtVO.TOKEN_PREFIX, "");

            // 토큰 검증(id, role 만 가지고 LoginUser 객체가 생성됨, 자세한건 JwtProcess.verify 메서드 참조)
            LoginUser loginUser = JwtProcess.verify(token);

            // 임세 세션, 패스워드 null 처리
            // UserDetails 타입 or username 을 첫번째 매개변수로 넣을 수 있다.
            // verify 메서드로 인해 현재 LoginUser 객체의 username 이 null 인 상태이므로
            // UserDetails 인터페이스를 구현한 LoginUser 객체를 그대로 첫번째 매개변수로 활용한다.
            // 어차피 핵심은 role 이다. role 만 잘 들어가 있으면 된다.
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null,
                    loginUser.getAuthorities());

            // 강제 로그인
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response); // FilterChain 다시 진행
    }

    // 토큰 존재여부 검증 메서드
    private boolean isHeaderVerify(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader(JwtVO.HEADER);
        // 헤더가 null 이거나 헤더의 시작값이 Bearer 가 아닌 경우
        if (header == null || !header.startsWith(JwtVO.TOKEN_PREFIX)) {
            return false;
        } else {
            return true;
        }
    }
}
