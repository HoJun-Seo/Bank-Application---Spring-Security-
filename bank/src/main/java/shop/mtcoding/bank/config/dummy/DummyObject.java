package shop.mtcoding.bank.config.dummy;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionEnum;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class DummyObject {

    // 진짜 유저를 만드는 메서드(Entity 를 실제로 save 할 때 사용)
    protected User newUser(String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");

        // id, 생성일자, 수정일자는 save 메서드가 동작하면 자동으로 User 객체에 생성되므로 제외시킨다.
        return User.builder()
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }

    // 가짜유저를 만드는 메서드(테스트를 위해 가짜, stub 을 만들 때 사용)
    // 실제로 save 메서드를 동작시키는 것이 아니기 때문에
    // id, 생성일자, 수정일자를 직접 할당해준다.
    protected User newMockUser(Long id, String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");

        return User.builder()
                .id(id)
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserEnum.CUSTOMER)
                .build();
    }

    protected Account newAccount(Long number, User user) {
        return Account.builder()
                .number(number)
                .password(1234L)
                .balance(1000L)
                .user(user)
                .build();
    }

    protected Account newMockAccount(Long id, Long number, Long balance, User user) {
        return Account.builder()
                .id(id)
                .number(number)
                .password(1234L)
                .balance(balance)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 계좌 잔액이 1100원이 되고 나서 입금 트랜잭션 히스토리가 생성되어야 함
    protected Transaction newMockDepositTransaction(Long id, Account account) {

        account.deposit(100L); // 거래내역에서 미리 매개변수로 넘어온 account 객체의 잔액을 늘려놓기
        // 테스트 때 accountDeposit 메서드의 반환값으로 돌아온 AccountDepositRespDto 객체가 가지고있는 잔액 값을
        // 검증해주기 위함
        return Transaction.builder()
                .id(id)
                .withdrawAccount(null) // 출금기능이 아니기에 출금 계좌는 null 값 고정
                .depositAccount(account)
                .amount(100L) // 입금금액 100원 고정, 굳이 매개변수를 통해 입금하고자 하는 값을 받지않음
                .withdrawAccountBalance(null) // 출금이후 계좌잔액값 null
                .depositAccountBalance(account.getBalance())
                .gubun(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(account.getNumber() + "")
                .tel("01022227777") // 입금자 전화번호값 고정
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
