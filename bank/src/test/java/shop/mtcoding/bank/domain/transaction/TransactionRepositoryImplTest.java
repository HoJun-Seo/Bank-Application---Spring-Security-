package shop.mtcoding.bank.domain.transaction;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;

@ActiveProfiles("test")
// Repository 테스트이기 @DataJpaTest 어노테이션을 사용하면 Mock 을 사용할 필요가없다.
@DataJpaTest // DB 관련된 Bean 이 다 올라온다.
public class TransactionRepositoryImplTest extends DummyObject {

    // TransactionRepository 가 Dao 인터페이스를 상속받고 있기 때문에 Dao 에서 정의해둔
    // findTransactionList 메서드를 호출하면
    // Dao 인터페이스의 구현체인 TransactionRepositoryImpl 에서 작성해둔 대로 메서드가 동작하게 된다.
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    public void setUp() {
        autoincrementReset(); // 기본키 증가값 초기화 메서드 호출
        dataSetting();
    }

    @Test
    public void findTransactionList_all_test() throws Exception {
        // given
        Long accountId = 1L;
        // when
        List<Transaction> transactionListPS = transactionRepository.findTransactionList(accountId, "ALL", 0);
        transactionListPS.forEach((t) -> {
            System.out.println("테스트 - 거래내역 Id : " + t.getId());
            System.out.println("테스트 - 거래금액 : " + t.getAmount());
            System.out.println("테스트 - 출금 계좌 : " + t.getSender());
            System.out.println("테스트 - 입금 계좌 : " + t.getReceiver());
            System.out.println("테스트 - 입금계좌 잔액 : " + t.getDepositAccountBalance());
            System.out.println("테스트 - 출금계좌 잔액 : " + t.getWithdrawAccountBalance());
            System.out.println("테스트 : =================================");
        });
        // then
    }

    @Test
    public void dataJpa_test_1() {
        List<Transaction> transactionList = transactionRepository.findAll();
        transactionList.forEach((transaction) -> {
            System.out.println("테스트 - 거래내역 id : " + transaction.getId());
            System.out.println("테스트 - 출금 : " + transaction.getSender());
            System.out.println("테스트 - 입금 : " + transaction.getReceiver());
            System.out.println("테스트 - 구분값 : " + transaction.getGubun());
            System.out.println("테스트 : ==================================");
        });
    }

    @Test
    public void dataJpa_test_2() {
        List<Transaction> transactionList = transactionRepository.findAll();
        transactionList.forEach((transaction) -> {
            System.out.println("테스트 - 거래내역 id : " + transaction.getId());
            System.out.println("테스트 - 출금 : " + transaction.getSender());
            System.out.println("테스트 - 입금 : " + transaction.getReceiver());
            System.out.println("테스트 - 구분값 : " + transaction.getGubun());
            System.out.println("테스트 : ==================================");
        });
    }

    private void dataSetting() {
        User ssar = userRepository.save(newUser("ssar", "쌀"));
        User cos = userRepository.save(newUser("cos", "코스,"));
        User love = userRepository.save(newUser("love", "러브"));
        User admin = userRepository.save(newUser("admin", "관리자"));

        Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
        Account cosAccount = accountRepository.save(newAccount(2222L, cos));
        Account loveAccount = accountRepository.save(newAccount(3333L, love));
        Account ssarAccount2 = accountRepository.save(newAccount(4444L, ssar));

        Transaction withdrawTransaction1 = transactionRepository
                .save(newWithdrawTransaction(ssarAccount1, accountRepository));
        Transaction depositTransaction1 = transactionRepository
                .save(newDepositTransaction(cosAccount, accountRepository));
        Transaction transferTransaction1 = transactionRepository
                .save(newTransferTransaction(ssarAccount1, cosAccount, accountRepository));
        Transaction transferTransaction2 = transactionRepository
                .save(newTransferTransaction(ssarAccount1, loveAccount, accountRepository));
        Transaction transferTransaction3 = transactionRepository
                .save(newTransferTransaction(cosAccount, ssarAccount1, accountRepository));
    }

    // 기본키 증가값 초기화 메서드
    // autoincrement 전략으로 인한 기본키 증가값도 초기화가 되어야 테스트에 용이하다.
    private void autoincrementReset() {
        em.createNativeQuery("ALTER TABLE user_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE account_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE transaction_tb ALTER COLUMN id RESTART WITH 1").executeUpdate();
    }
}
