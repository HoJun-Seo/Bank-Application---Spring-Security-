package shop.mtcoding.bank.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;

@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // 시큐리티로 로그인 될때 시큐리티가 실행해서 DB 에 username 이 존재하는지 체크하는 메서드
    // username 이 없는 경우 오류발생, 있는 경우 정상적으로 SecurityContext 내부 세션에 로그인된 세션이 만들어짐
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userPS = userRepository.findByUsername(username).orElseThrow(
                () -> new InternalAuthenticationServiceException("인증 실패"));

        return new LoginUser(userPS);
    }

    // 시큐리티를 통한 인증이 진행되고 있다가 에러가 발생하면 이 상황에선 개발자가 제어권을 가지고 있지 않기 때문에
    // InternalAuthenticationServiceException 와 같은 익셉션을 호출시켜서 터트려준다.
    // 추후에 테스트가 수행될 것

}
