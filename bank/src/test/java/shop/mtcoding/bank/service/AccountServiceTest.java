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

    @Test
    public void depositAccount_test() {
        // given
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01022227777");

        User user = newMockUser(1L, "ssar", "쌀");
        Account account = newMockAccount(1L, 1111L, 1000L, user);
        Transaction transaction = newMockTransaction(1L, null, account, accountDepositReqDto.getAmount(), null,
                account.getBalance() + accountDepositReqDto.getAmount(), TransactionEnum.DEPOSIT, "ATM",
                account.getNumber() + "",
                accountDepositReqDto.getTel());

        // stub 1
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(account));

        // stub 2
        when(transactionRepository.save(any())).thenReturn(transaction);
        // when
        AccountDepositRespDto accountDepositRespDto = accountService.accountDeposit(accountDepositReqDto);
        // then
        assertThat(accountDepositRespDto.getNumber()).isEqualTo(account.getNumber());
        assertThat(accountDepositRespDto.getTransactionDto().getAmount()).isEqualTo(accountDepositReqDto.getAmount());
        assertThat(accountDepositRespDto.getTransactionDto().getGubun()).isEqualTo(TransactionEnum.DEPOSIT.getValue());
        assertThat(accountDepositRespDto.getTransactionDto().getTel()).isEqualTo(accountDepositReqDto.getTel());
        System.out
                .println(
                        "테스트 - 거래이후 입금계좌 잔액 : " + accountDepositRespDto.getTransactionDto().getDepositAccountBalance());
        System.out.println("테스트 - 거래이후 입금계좌 잔액 예상 : " + (account.getBalance()));
        /*
         * accountRepository.findByNumber 메서드의 결과값으로 위에서 만든 Account 객체를 돌려받게 해두었는데,
         * accountService.accountDeposit 메서드가 실행되는 과정에서 Account 객체의 deposit 메서드가 실행되었고
         * 이 과정에서 위에서 만들어둔 Account 객체의 balance 값이 실제로 변경되어버림
         * (Account.deposit 메서드는 void 타입이기 때문에 stub 으로 거짓 반환값을 만들어주기가 힘들다.)
         * 그렇기 때문에 거래이후 잔액을 검증할 때 account 객체의 잔액에 입금요청 금액을 더한값으로 검증하는 것이 아니라,
         * account 객체의 잔액 그 자체로 검증해야함(deposit 메서드 호출로 인해 실제로 객체의 잔액 값이 변경되었기 때문)
         */
        assertThat(accountDepositRespDto.getTransactionDto().getDepositAccountBalance())
                .isEqualTo(account.getBalance());
        assertThat(accountDepositRespDto.getTransactionDto().getSender()).isEqualTo("ATM");
        assertThat(accountDepositRespDto.getTransactionDto().getReceiver())
                .isEqualTo(accountDepositReqDto.getNumber() + "");
    }
}
