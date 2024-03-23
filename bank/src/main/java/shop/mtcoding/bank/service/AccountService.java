package shop.mtcoding.bank.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionEnum;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountWithdrawReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountWithdrawRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    // 거래내역을 남기기 위한 TransactionRepository 의존성 주입
    private final TransactionRepository transactionRepository;

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

    /*
     * ATM -> 누군가의 계좌로 입금
     */
    // 따로 인증이 필요없다.(ATM 기기에서 송금하는 것으로 간주하고 있기때문)
    @Transactional
    public AccountDepositRespDto accountDeposit(AccountDepositReqDto accountDepositReqDto) {
        // 0원 체크(amount == 0)
        // validation 과정에서 검증해도 무관하나, 서비스에서도 한번 해보자.
        if (accountDepositReqDto.getAmount() <= 0) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
        // 입금 계좌가 존재하는지 확인
        Account depositAccountPS = accountRepository.findByNumber(accountDepositReqDto.getNumber())
                .orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다."));
        // 입금(해당 계좌의 balance 값 조정 - update query - 더티 체킹이 발생함)
        depositAccountPS.deposit(accountDepositReqDto.getAmount());
        // 거래내역 남기기
        Transaction transaction = Transaction.builder()
                .depositAccount(depositAccountPS)
                .withdrawAccount(null) // ATM 기기에서 입금한다는 설정이므로 출금 계좌는 없음
                .depositAccountBalance(depositAccountPS.getBalance())
                .withdrawAccountBalance(null)
                .amount(accountDepositReqDto.getAmount())
                .gubun(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(accountDepositReqDto.getNumber() + "") // 계좌번호를 문자열 타입으로 변환
                .tel(accountDepositReqDto.getTel())
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        return new AccountDepositRespDto(depositAccountPS, transactionPS);
    }

    // 로그인이 되어있어야 하기 때문에 userId 를 받는다.
    @Transactional
    public AccountWithdrawRespDto accountWithdraw(AccountWithdrawReqDto accountWithdrawReqDto, Long userId) {
        // 0원 체크
        if (accountWithdrawReqDto.getAmount() <= 0) {
            throw new CustomApiException("0원 이하의 금액을 출금할 수 없습니다.");
        }

        // 출금계좌 확인
        Account withdrawAccountPS = accountRepository.findByNumber(accountWithdrawReqDto.getNumber())
                .orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다."));

        // 출금계좌 소유자 확인(로그인한 사람과 계좌 소유자가 동일한지 체크)
        withdrawAccountPS.checkOwner(userId);
        // 출금계좌 비밀번호 확인
        withdrawAccountPS.checkSamePassword(accountWithdrawReqDto.getPassword());
        // 출금계좌 잔액 확인
        withdrawAccountPS.checkBalance(accountWithdrawReqDto.getAmount());
        // 출금하기
        withdrawAccountPS.withdraw(accountWithdrawReqDto.getAmount());
        // 거래내역 남기기(내 계좌에서 ATM으로 출금)
        // 전화번호 정보(tel) 는 필요하지 않음
        Transaction transaction = Transaction.builder()
                .depositAccount(null)
                .withdrawAccount(withdrawAccountPS)
                .depositAccountBalance(null)
                .withdrawAccountBalance(withdrawAccountPS.getBalance()) // 내 계좌 잔액은 볼 수 있어야 함, 응답객체에 @JsonIgnore가 들어가면
                                                                        // 안됨
                .amount(accountWithdrawReqDto.getAmount())
                .gubun(TransactionEnum.WITHDRAW)
                .sender(accountWithdrawReqDto.getNumber() + "")
                .receiver("ATM")
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);
        // 응답객체 반환하기
        return new AccountWithdrawRespDto(withdrawAccountPS, transaction);
    }
}
