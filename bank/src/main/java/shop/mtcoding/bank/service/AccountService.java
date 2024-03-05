package shop.mtcoding.bank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // 데이터베이스의 변경을 요구하는 메서드이기 때문에
    // @Transactional 어노테이션 등록
    // User 로그인되어 있는 상태여야함
    // 로그인 되어있는지에 대한 확인은 Service 가 할일이 아님(컨트롤러가 할 일)
    @Transactional
    public void accountRegister(AccountSaveReqDto accountSaveReqDto, Long userId) {
        // User 가 DB에 있는지 검증을 위한 엔티티 필요
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다."));

        // 해당 계좌가 DB에 있는지 중복여부 체크
        accountRepository.findByNumber(accountSaveReqDto.getNumber());

        // 계좌 등록

        // DTO를 응답
    }

    // 계좌등록 요청 시 활용할 DTO
    // 추후에 다른 클래스로 옮길것
    @Getter
    @Setter
    public static class AccountSaveReqDto {

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long password;

        // 잔액(초기 잔액 1000원)과 user 정보는 받을 필요없음
        // user 정보는 세션에서 가져오면 됨
    }
}
