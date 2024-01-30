package shop.mtcoding.bank.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // Jpa 의 Name Query(Query Creation) 가 작동하여 자동으로 쿼리 생성
    Optional<User> findByUsername(String username);

}
