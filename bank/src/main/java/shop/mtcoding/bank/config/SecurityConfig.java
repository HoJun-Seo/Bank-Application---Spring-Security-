package shop.mtcoding.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import shop.mtcoding.bank.config.jwt.JwtAuthenticationFilter;
import shop.mtcoding.bank.config.jwt.JwtAuthorizationFilter;
import shop.mtcoding.bank.domain.user.UserEnum;
import shop.mtcoding.bank.util.CustomResponseUtil;

@Configuration
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그 : BcryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {

        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
            builder.addFilter(new JwtAuthorizationFilter(authenticationManager)); // 인가필터 추가
            super.configure(builder);
        }
    }

    @Bean
    // JWT 서버를 만들 예정이기 때문에 세션은 사용하지 않는다.(시큐리티 세션 사용)
    public <T> SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("디버그 : filterChain 빈 등록됨");

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        http.csrf(csrf -> csrf.disable()); // enable 이면 포스트맨이 동작하지 못함
        http.cors(cors -> cors.configurationSource(configurationSource())); // cors 를 승인해두지 않으면 컨트롤러에 자바스크립트로 전송되는 요청을
                                                                            // 모두 막음

        // 세션 id 를 서버쪽에서 관리하지 않겠다는 뜻
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.formLogin(form -> form.disable());
        http.httpBasic(httpBasic -> httpBasic.disable());

        // JWT 필터 등록
        http.with(new CustomSecurityFilterManager(), customizer -> customizer.getClass());

        // 인증실패 처리
        http.exceptionHandling(handle -> handle.authenticationEntryPoint((request, response, authException) -> {
            CustomResponseUtil.fail(response, "로그인을 진행해주세요", HttpStatus.UNAUTHORIZED);
        }));

        // 권한실패 처리
        http.exceptionHandling(handle -> handle.accessDeniedHandler((request, response, e) -> {
            CustomResponseUtil.fail(response, "권한이 없습니다.", HttpStatus.FORBIDDEN);
        }));
        // 최근 공식문서에서는 ROLE_ 붙이지 않아도 됨
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/api/s/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("" + UserEnum.ADMIN)
                .anyRequest().permitAll());

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        log.debug("디버그 : configurationSource cors 설정이 SecurityFilterChain 에 등록됨");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE, (자바스크립트 요청까지 허용)
        configuration.addAllowedOriginPattern("*"); // 프론트엔드 IP 만 허용, react 등
        // 휴대폰 앱 같은 경우 모든 사용자들이 다른 ip 를 사용하기 때문에
        // 허용하고 아니고와 같은걸 고려할 수가 없다.
        // 휴대폰 앱 자체에서도 자바스크립트로만 요청하는 것이 아니다.
        // 자바 또는 스위프트와 같은 언어로 요청이 들어오기 때문에 애초에 cors 에 걸리지 않는다.
        // 그렇기 때문에 휴대폰 앱에 대한 ip 허용 여부는 고려되지 않는다.
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용(지금은 굳이 필요없음)
        configuration.addExposedHeader("Authorization"); // 헤더에 Authorization 속성값 노출
        // 모든 주소 요청에 위의 설정을 추가한다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
