package shop.mtcoding.bank.config.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.dto.user.UserReqDto.LoginReqDto;
import shop.mtcoding.bank.dto.user.UserRespDto.LoginRespDto;
import shop.mtcoding.bank.util.CustomResponseUtil;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(getClass()); // 필터 동작확인을 위한 로그 추가
    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
    }

    // Post /login 요청이 들어왔을 경우 메서드 동작한다.
    // 이때 매개변수 request, response 에 각각 요청, 응답 정보가 담긴다.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // attemptAuthentication 메서드 호출 확인용 디버그
        log.debug("디버그 : attemptAuthentication 호출됨");

        try {
            // request 내부에 json 타입의 데이터가 있기 때문에 이를 ObjectMapper 를 통해서 꺼낸다.
            ObjectMapper om = new ObjectMapper();

            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            // 강제 로그인 진행
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginReqDto.getUsername(), loginReqDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken); // 강제 로그인
            // authenticationManager.authenticate() 메서드가 내부에서 호출하는 건 UserDetailsService 의
            // loadUserByUsername 메서드이다.
            return authentication;
        } catch (Exception e) {
            // 시큐리티 로그인 과정 진행도중 익셉션 발생 시
            throw new InternalAuthenticationServiceException(e.getMessage());

            // InternalAuthenticationServiceException 익셉션 발생 시
            // unsuccessfulAuthentication 메서드가 간접적으로 호출됨
        }
    }

    // return authentication 이 잘 동작하면(attemptAuthentication 메서드가 잘 동작하면) 호출되는 메서드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        // successfulAuthentication 메서드 호출 확인용 디버그
        log.debug("디버그 : successfulAuthentication 호출됨");
        // loadUserByUsername 메서드의 호출 결과로 DB 에서 가져온 로그인된 유저 정보 가져오기
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String jwtToken = JwtProcess.create(loginUser);
        response.addHeader(JwtVO.HEADER, jwtToken);

        // 로그인 이후 응답으로 돌려줄 DTO 필요
        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        CustomResponseUtil.success(response, loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.unAuthentication(response, "로그인 실패");
    }

}
