package shop.mtcoding.bank.config.jwt;

import java.io.IOException;

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

            // 강제로 로그인을 진행하는 이유는 아무리 JWT 를 사용한다고 해도 요청을 처리하기 위해선 컨트롤러에 진입해야 하는데
            // 시큐리티에 권한체크, 인증체크를 해주는 (http.authorizeRequests()) 기능의 도움을 받기위해 세션을 만들어야 하기
            // 때문이다.
            // 이 세션의 유효기간은 request -> response 가 진행되고 나면 끝난다.(임시 세션일 뿐임)
            // 클라이언트가 요청 이후 응답을 돌려뱓고나면 이 세션은 더 이상 의미가 없어진다.
            // 다음 요청때 이 세션을 사용하는 것도 불가능하다. 왜냐하면 jSessionId 를 가지고 있지 않기 때문이다.
            return authentication;
        } catch (Exception e) {
            // 시큐리티 로그인 과정 진행도중 익셉션 발생 시
            throw new InternalAuthenticationServiceException(e.getMessage());
            // 이 에러가 던져지면 SecurityConfig 에서 http.exceptionHandling() 을 통해 설정했던
            // authenticationEntryPoint 에 걸려서
            // CustomResponseUtil.unAuthentication() 메서드가 동작해서 익셉션을 발생시키게 된다.

            // 시큐리티 필터에서 발생한 에러이기 때문에 ControllerAdvice 로 넘길수가 없다.
            // 애초에 필터를 모두 통과해야 컨트롤러로 넘어가는 건데 그 전부터 에러가 발생한것이기 때문에 당연한것이다.
        }
    }

    // return authentication 이 잘 동작하면(attemptAuthentication 메서드가 잘 동작하면) 호출되는 메서드
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        // loadUserByUsername 메서드의 호출 결과로 DB 에서 가져온 로그인된 유저 정보 가져오기
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String jwtToken = JwtProcess.create(loginUser);
        response.addHeader(JwtVO.HEADER, jwtToken);

        // 로그인 이후 응답으로 돌려줄 DTO 필요
        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        CustomResponseUtil.success(response, loginRespDto);
    }
}
