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
        User user = newMockUser(1L, "ssar", "쌀");
        Account account1 = newMockAccount(1L, 1111L, 1000L, user);
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(account1));

        // stub 2
        Account account2 = newMockAccount(1L, 1111L, 1000L, user);
        Transaction transaction = newMockDepositTransaction(1L, account2);
        when(transactionRepository.save(any())).thenReturn(transaction);

        /*
         * 두개의 stub 에 하나의 객체를 동시에 사용하면 객체의 값이 꼬이게 되는 경우가 있다.
         * 지금 현재의 테스트만 봐도 하나의 account 객체를 이용해 두 개의 stub 에 모두 활용하면
         * 하나의 account 객체에 대해 Transaction 객체가 만들어질때 deposit 메서드가 한번,
         * 그리고 accountService.accountDeposit 메서드에서 deposit 메서드가 또 한번 실행되어
         * 하나의 account 객체에 총 두번의 deposit 메서드가 호출되어서 입금이 두번되는 경우가 생긴다.
         * 
         * 그런데 위와 같이 각각의 stub 에 대해 필요한 객체를 따로따로 만들어주면
         * accountService.accountDeposit 메서드 호출때는 account1 객체에 대해 deposit 이 이루어지고
         * transaction 객체가 생성될 때 호출되는 deposit 메서드는 account2 객체에 대해 이루어지기 때문에
         * 하나의 객체에 반복적으로 똑같은 비즈니스 메서드가 호출되는 것을 막을 수 있다.
         * 
         * 이를 통해 응답으로 돌아온 AccountDepositRespDto 객체와 transaction 객체간의 입금후 잔액 비교뿐만이 아니라
         * stub 으로 사용한 account 객체에 대해서도 입금후 잔액 비교가 가능해진다.
         */
        // when
        AccountDepositRespDto accountDepositRespDto = accountService.accountDeposit(accountDepositReqDto);
        // then
        assertThat(accountDepositRespDto.getNumber()).isEqualTo(account1.getNumber());
        assertThat(accountDepositRespDto.getTransactionDto().getAmount()).isEqualTo(accountDepositReqDto.getAmount());
        assertThat(accountDepositRespDto.getTransactionDto().getGubun()).isEqualTo(TransactionEnum.DEPOSIT.getValue());
        assertThat(accountDepositRespDto.getTransactionDto().getTel()).isEqualTo(accountDepositReqDto.getTel());
        assertThat(accountDepositRespDto.getTransactionDto().getDepositAccountBalance())
                .isEqualTo(transaction.getDepositAccountBalance());
        assertThat(accountDepositRespDto.getTransactionDto().getDepositAccountBalance())
                .isEqualTo(account1.getBalance());
        assertThat(accountDepositRespDto.getTransactionDto().getDepositAccountBalance())
                .isEqualTo(account2.getBalance());
        System.out.println(
                "테스트 - 응답 객체 입금후 잔액 : " + accountDepositRespDto.getTransactionDto().getDepositAccountBalance());
        System.out.println(
                "테스트 - 거래내역 객체 입금 후 잔액 : " + transaction.getDepositAccountBalance());
        System.out.println(
                "테스트 - account1 객체 입금 후 잔액(accountDeposit 메서드에서 deposit 메서드 호출) : "
                        + account1.getBalance());
        System.out.println(
                "테스트 - account2 객체 입금 후 잔액(transaction 객체 생성 시 내부에서 deposit 메서드 호출) : "
                        + account2.getBalance());

        /*
         * accountDeposit 메서드 내부에서 account 객체에 대해 deposit 메서드가 호출되기는 하나,
         * 호출됨으로서 변경된 account 객체의 balance 값은 AccountDepositRespDto 객체가 만들어질 때 반영되지 않음
         * 반환받은 가짜 transaction 객체가 가지고있는 입금계좌 잔액이 반영될 뿐,
         * 이때 가짜 transaction 객체가 생성될 때 이미 내부에서 account 객체에 대한 deposit 이 완료된 이후의 잔액이 저장됨
         * 결국 이미 deposit 이 완료된 계좌 잔액을 들고 있는 transaction 객체를 통해 AccountDepositRespDto 객체가
         * 생성되는 것.
         */
        assertThat(accountDepositRespDto.getTransactionDto().getSender()).isEqualTo("ATM");
        assertThat(accountDepositRespDto.getTransactionDto().getReceiver())
                .isEqualTo(accountDepositReqDto.getNumber() + "");
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
}
