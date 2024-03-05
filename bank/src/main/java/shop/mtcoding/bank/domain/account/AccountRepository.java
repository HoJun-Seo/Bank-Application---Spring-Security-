package shop.mtcoding.bank.domain.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // JPA query 메서드이므로 Hibernate 에서 자동으로 쿼리를 만들어준다.
    // 추후에 User 객체까지 함께 정보를 가져와야 할 때 현재 User 객체를 가져오는 것에 대해서는 LAZY 전략을 채택하고 있으므로
    // LAZY 전략과 상관없이 조회하는 대로 곧장 User 객체를 가져와야 할 필요가 있을 경우 직접 쿼리를 작성해주어야 한다.
    // checkpoint : 추후 리팩토링 할 것
    Optional<Account> findByNumber(Long number);
}
