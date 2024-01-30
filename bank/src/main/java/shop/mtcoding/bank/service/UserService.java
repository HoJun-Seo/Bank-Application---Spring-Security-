package shop.mtcoding.bank.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // 동일 유저 검색, 유저 저장등의 기능 수행을 위한 의존성 주입
    private final UserRepository userRepository;

    // 메서드가 시작될 때 트랜잭션이 함께 시작되고
    // 메서드가 종료될 때 함께 트랜잭션이 종료된다.
    @Transactional
    public void userRegister(JoinReDto joinReDto) {
        // 1. 동일 유저네임 존재 검색
        Optional<User> userOp = userRepository.findByUsername(joinReDto.getUsername());
        if (userOp.isPresent()) {
            // 조건문이 동작했다면 중복되는 유저네임이 존재한다는 뜻
        }

        // 2. 패스워드 인코딩

        // 3. DTO 로 응답
    }

    @Getter
    @Setter
    public static class JoinReDto {
        // 유효성 검사
        private String username;
        private String password;
        private String email;
        private String fullname;
    }
}
