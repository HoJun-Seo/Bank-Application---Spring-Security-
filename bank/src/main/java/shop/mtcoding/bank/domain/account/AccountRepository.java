package shop.mtcoding.bank.domain.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // JPA query 메서드이므로 Hibernate 에서 자동으로 쿼리를 만들어준다.
    Optional<Account> findByNumber(Long number);

    // 본인 계좌목록 보기
    // jpa query method
    // select * from account where user_id = :id
    List<Account> findByUser_id(Long id);
}
