package shop.mtcoding.bank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.service.UserService.JoinReDto;
import shop.mtcoding.bank.service.UserService.JoinRespDto;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Spy // 진짜 스프링 컨테이너에 등록되어 있는 빈을 userService(가짜) 에 주입해준다.
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 회원가입_test() throws Exception {
        // given
        JoinReDto joinReDto = new JoinReDto();
        joinReDto.setUsername("ssar");
        joinReDto.setPassword("1234");
        joinReDto.setEmail("ssar@nate.com");
        joinReDto.setFullname("쌀");

        // stub : 가정법, 가설

        // 어떠한 username 값이 매개변수로 findByUsername 메서드가 수행되어도 Optional.empty() 를 반환하라.
        // 즉, 어떠한 username 값으로 회원정보를 찾아도, 중복여부와 상관없이 지정된 값을 반환해주라는 뜻이다.
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        // stub
        User ssar = User.builder()
                .id(1L)
                .username("ssar")
                .password("1234")
                .email("ssar@nate.com")
                .fullname("쌀")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserEnum.CUSTOMER)
                .build();
        when(userRepository.save(any())).thenReturn(ssar);

        // when
        JoinRespDto joinRespDto = userService.userRegister(joinReDto);
        System.out.println("테스트 : " + joinRespDto);

        // then
        assertThat(joinRespDto.getId()).isEqualTo(1L);
        assertThat(joinRespDto.getUsername()).isEqualTo("ssar");
    }
}
