package shop.mtcoding.bank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionEnum;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountTransferReqDto;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountWithdrawReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest extends DummyObject {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    // @Mock 어노테이션이 달려있는 객체들은 모두 @InjectMocks 어노테이션이 붙어있는 객체에 주입된다(DI).

    @Spy
    private ObjectMapper om;

    @Test
    public void accountRegister_test() throws Exception {
        // given
        Long userId = 1L;
        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(1111L);
        accountSaveReqDto.setPassword(1234L);

        // stub 1
        User user = newMockUser(userId, "ssar", "쌀");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        // stub 2
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty()); // 중복되는 계좌정보가 없어야 하므로
        // stub 3
        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, user);
        when(accountRepository.save(any())).thenReturn(ssarAccount);

        // when
        AccountSaveRespDto accountSaveRespDto = accountService.accountRegister(accountSaveReqDto, userId);
        String responseBody = om.writeValueAsString(accountSaveRespDto);
        System.out.println("테스트 : " + responseBody);
        // then
        assertThat(accountSaveRespDto.getNumber()).isEqualTo(1111L);
    }

    @Test
    public void searchAccountListByUser_test() throws Exception {
        // given
        Long userId = 1L;
        User user = newMockUser(userId, "ssar", "쌀");

        List<Account> accountList = new ArrayList<>();
        for (Long i = 1L; i < 3L; i++) {
            accountList.add(newMockAccount(i, 1110L + i, 1000L * i, user));
        }
        accountList.stream()
                .forEach((account) -> System.out
                        .println("테스트 - id : " + account.getId() + ", number : " + account.getNumber()));

        // stub 1
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // stub 2
        when(accountRepository.findByUser_id(userId)).thenReturn(accountList);
        // when
        AccountListRespDto accountListRespDto = accountService.searchAccountListByUser(userId);
        // then
        assertThat(accountListRespDto.getFullname()).isEqualTo(user.getFullname());
        assertThat(accountListRespDto.getAccounts().size()).isEqualTo(2);
    }

    @Test
    public void deleteAccount_test() {
        // given
        Long number = 1111L;
        Long userId = 2L;

        // stub
        // userId 와 다른 값을 id 로 지정(계좌 소유자를 다르게 하기 위함)
        User user = newMockUser(1L, "ssar", "쌀");
        Account ssarAccount = newMockAccount(1L, number, 1000L, user);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount));

        // accountRepository.deleteById 메서드의 경우 실행결과 반환값을 돌려주는 것이 아니므로
        // 굳이 stub 을 만들어줄 필요가 없다.

        // when

        // then
        // accountRepository.deleteById 메서드가 잘 터지는지에 대한 여부는 테스트할 필요가 없다.
        // 어차피 JPA 개발진들이 알아서 잘 테스트하고 있을 것이다.
        // 그러므로 계좌 소유자가 동일한지 아닌지에 대해 검증이 잘 되는지 확인해보는 테스트를 만들어보자.
        assertThrows(CustomApiException.class, () -> accountService.deleteAccount(number, userId));

    }

    /*
     * 이 테스트 메서드의 목적
     * 1. 계좌입금이 이루어진 이후 응답으로 돌아온 객체의 balance 값 확인
     * 2. Transaction 객체(거래내역 객체)에 balance 값이 잘 저장되었는지
     */
    @Test
    public void depositAccount_test() {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01022227777");

        // stub 1
        User user1 = newMockUser(1L, "ssar", "쌀");
        Account account1 = newMockAccount(1L, 1111L, 1000L, user1);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(account1));

        // stub 2
        User user2 = newMockUser(1L, "ssar", "쌀");
        Account account2 = newMockAccount(1L, 1111L, 1000L, user2);
        Transaction transaction = newMockDepositTransaction(1L, account2);
        when(transactionRepository.save(any())).thenReturn(transaction);

        // when
        AccountDepositRespDto accountDepositRespDto = accountService.accountDeposit(accountDepositReqDto);
        // then
        assertThat(accountDepositRespDto.getTransactionDto().getDepositAccountBalance()).isEqualTo(1100L);
    }

    // 위의 계좌입금 서비스 테스트 코드 간소화(책임 소재 명확하게 하기)
    /*
     * 계좌입금 서비스에서는 계좌입금 기능이 잘 동작했는지, 0원 처리가 잘 되었는지만 확인하면 됐지,
     * findByNumber 가 잘 동작했는지, Transaction 객체가 잘 만들어졌는지 여부는 굳이 확인할 필요는 없음
     * (transaction 객체가 잘 만들어졌는지는 컨트롤러를 테스트할 때도 확인 가능함)
     */
    @Test
    public void depositAccount_simplify() {
        // given
        // user 객체도 굳이 필요없음
        Account account = newMockAccount(1L, 1111L, 1000L, null);
        Long amount = 100L;

        // when
        if (amount <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
        account.deposit(100L);

        // then
        assertThat(account.getBalance()).isEqualTo(1100L);
    }

    @Test
    public void depositAccount_amountError_test() {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(0L); // 입금 금액이 0원인 경우
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01022227777");

        // when

        // then
        assertThrows(CustomApiException.class, () -> accountService.accountDeposit(accountDepositReqDto));
    }

    @Test
    public void depositAccount_accountSearchError_test() {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01022227777");

        // when
        // 존재하지 않는 계좌번호를 검색했을 경우
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty());
        // then
        assertThrows(CustomApiException.class, () -> accountService.accountDeposit(accountDepositReqDto));
    }

    @Test
    public void withdrawAccount_test() throws Exception {
        // given
        Long amount = 100L;

        User user = newMockUser(1L, "ssar", "쌀");
        Account account = newMockAccount(1L, 1111L, 1000L, user);
        // when
        // 0원 체크
        if (amount <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 출금할 수 없습니다.");
        }
        // 출금계좌 소유자 확인
        account.checkOwner(user.getId());
        // 비밀번호 확인
        account.checkSamePassword(1234L); // newMockAccount 메서드 내부에서 설정하는 계좌 비밀번호
        // 잔액 확인
        account.checkBalance(amount);
        // 출금하기
        account.withdraw(amount);

        // then
        assertThat(account.getBalance()).isEqualTo(900L);
    }

    // 계좌이체 테스트
    @Test
    public void transferAccount_test() {

        // given
        Long userId = 1L;
        AccountTransferReqDto accountTransferReqDto = new AccountTransferReqDto();
        accountTransferReqDto.setWithdrawNumber(1111L);
        accountTransferReqDto.setDepositNumber(2222L);
        accountTransferReqDto.setWithdrawPassword(1234L);
        accountTransferReqDto.setAmount(100L);
        accountTransferReqDto.setGubun("TRANSFER");

        User user = newMockUser(1L, "ssar", "쌀");
        Account withdrawAccount = newMockAccount(1L, 1111L, 1000L, user);
        Account depositAccount = newMockAccount(2L, 2222L, 1000L, null);

        // when
        // 출금계좌와 입금 계좌가 동일하면 안됨
        if (accountTransferReqDto.getWithdrawNumber().longValue() == accountTransferReqDto.getDepositNumber()
                .longValue()) {
            throw new CustomApiException("입출금 계좌가 동일할 수 없습니다.");
        }

        // 0원 체크
        if (accountTransferReqDto.getAmount() <= 0) {
            throw new CustomApiException("0원 이하의 금액을 출금할 수 없습니다.");
        }

        withdrawAccount.checkOwner(userId);
        // 출금계좌 비밀번호 확인
        withdrawAccount.checkSamePassword(accountTransferReqDto.getWithdrawPassword());
        // 출금계좌 잔액 확인
        withdrawAccount.checkBalance(accountTransferReqDto.getAmount());
        // 이체하기
        withdrawAccount.withdraw(accountTransferReqDto.getAmount());
        depositAccount.deposit(accountTransferReqDto.getAmount());
        // then
        assertThat(withdrawAccount.getBalance()).isEqualTo(900L);
        assertThat(depositAccount.getBalance()).isEqualTo(1100L);
    }
}
