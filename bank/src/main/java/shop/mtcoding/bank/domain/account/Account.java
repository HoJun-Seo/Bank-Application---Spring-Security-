package shop.mtcoding.bank.domain.account;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@EntityListeners(AuditingEntityListener.class)
@Table(name = "account_tb")
@Entity
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실제 구현을 위한것이 아닌 학습용이기에 편의성을 위해서 최대길이 4자로 설정
    @Column(unique = true, nullable = false, length = 4)
    private Long number; // 계좌번호

    @Column(nullable = false, length = 4)
    private Long password; // 계좌 비밀번호

    @Column(nullable = false)
    private Long balance; // 잔액 (기본값 : 1000 원)

    // 항상 ORM에서 fk 의 주인은 Many Entity 쪽이다.
    // Account 를 조회했을 때 다른 정보들은 바로 가져오나
    // user 의 경우 실제 DB 에서는 user 의 id 값이 저장되는데
    // 아래와 같이 LAZY 전략을 지정해두면 필요해서 호출하기 전까지는 해당되는 user 객체 정보를 불러오지 않는다.
    // account.getUser().아무필드호출() -> LAZY 발동, getUser() 까지는 LAZY 가 발동하지 않는다.
    // 만약 Eager 전략이었다면 Account 를 조회할 때 바로 join 을 통해 User 정보를 가져온다.
    // LAZY 전략을 사용하는 이유는 데이터 join 의 제어권을 개발자가 쥐겠다는 뜻이다.
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    // 특별히 설정을 해두지 않으면 테이블이 만들어질 때
    // 해당 필드의 경우 user_id 라는 이름으로 컬럼이 만들어진다.

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Account(Long id, Long number, Long password, Long balance, User user, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.number = number;
        this.password = password;
        this.balance = balance;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void checkOwner(Long userId) {
        if (user.getId() != userId) {
            throw new CustomApiException("계좌 소유자가 아닙니다.");
        }
    }
}
