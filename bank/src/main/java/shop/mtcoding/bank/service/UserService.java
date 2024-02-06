package shop.mtcoding.bank.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.user.UserReqDto.JoinReqDto;
import shop.mtcoding.bank.dto.user.UserRespDto.JoinRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // 동일 유저 검색, 유저 저장등의 기능 수행을 위한 의존성 주입
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 서비스는 DTO 를 요청받고 DTO 로 응답한다.

    // 메서드가 시작될 때 트랜잭션이 함께 시작되고
    // 메서드가 종료될 때 함께 트랜잭션이 종료된다.
    @Transactional
    public JoinRespDto userRegister(JoinReqDto joinReqDto) {
        // 1. 동일 유저네임 존재 검색
        Optional<User> userOp = userRepository.findByUsername(joinReqDto.getUsername());
        if (userOp.isPresent()) {
            // 조건문이 동작했다면 중복되는 유저네임이 존재한다는 뜻
            throw new CustomApiException("동일한 username 이 존재합니다.");
        }

        // 2. 패스워드 인코딩
        // PS : Persistence
        User userPS = userRepository.save(joinReqDto.toEntity(passwordEncoder));

        // 3. DTO 로 응답
        return new JoinRespDto(userPS);
    }
}
