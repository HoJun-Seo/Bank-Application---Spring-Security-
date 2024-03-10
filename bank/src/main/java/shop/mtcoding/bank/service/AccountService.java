package shop.mtcoding.bank.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // 사용자별 계좌목록 보기
    public AccountListRespDto searchAccountListByUser(Long userId) {
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다."));

        // 사용자의 모든 계좌목록 조회
        List<Account> accountListPS = accountRepository.findByUser_id(userPS.getId());

        // 응답으로 돌려줄 객체 AccountListRespDto 생성 및 반환
        return new AccountListRespDto(userPS, accountListPS);
    }

    // 데이터베이스의 변경을 요구하는 메서드이기 때문에
    // @Transactional 어노테이션 등록
    // User 로그인되어 있는 상태여야함
    // 로그인 되어있는지에 대한 확인은 Service 가 할일이 아님(컨트롤러가 할 일)
    @Transactional
    public AccountSaveRespDto accountRegister(AccountSaveReqDto accountSaveReqDto, Long userId) {
        // User 가 DB에 있는지 검증을 위한 엔티티 필요
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다."));

        // 해당 계좌가 DB에 있는지 중복여부 체크
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if (accountOP.isPresent()) {
            throw new CustomApiException("해당 계좌가 이미 존재합니다.");
        }

        // 계좌 등록
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(userPS));

        // DTO를 응답
        return new AccountSaveRespDto(accountPS);
    }

    @Transactional
    public void deleteAccount(Long number, Long userId) {
        // 1. 실제로 존재하는 계좌인지 확인
        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다."));
        // 2. 계좌 소유자 확인
        accountPS.checkOwner(userId);
        // 3. 계좌 삭제
        accountRepository.deleteById(accountPS.getId());
        // 계좌 삭제만 하면 되기 때문에 응답으로 돌려줄 DTO 는 필요하지 않다.
    }
}
