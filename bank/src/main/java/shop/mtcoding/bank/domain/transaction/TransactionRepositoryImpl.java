package shop.mtcoding.bank.domain.transaction;

import java.util.List;

import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

/*
 * 인터페이스 Dao 에서 정의해준 findTransactionList 메서드는 TransactionRepository 에서도 정의해줄 수도 있다.
 * TransactionRepository 에서 @Query 어노테이션을 통해 findTransactionList 메서드에서 동작시킬 쿼리를 정의해줄 수 있는데,
 * 이렇게 되면 동적 쿼리를 작성할 수 없게된다.
 */
interface Dao {
    List<Transaction> findTransactionList(@Param("accountId") Long accountId, @Param("gubun") String gubun,
            @Param("page") Integer page);
}

// 입출금내역 조회 서비스를 위해 TransactionRepository 에 대한 구현체 클래스 생성
// 동적쿼리를 사용한다.
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements Dao {

    private final EntityManager em;

    @Override
    public List<Transaction> findTransactionList(Long accountId, String gubun, Integer page) {
        // 동적쿼리 생성(gubun 값을 가지고 동적쿼리 생성)
        // DEPOSIT, WITHDRAW, ALL 등 gubun 값이 무엇이냐에 따라 자바 문법을 활용하여 동적쿼리를 생성한다.
        String sql = "";
        sql += "select t from Transaction t "; // JPQL

        if (gubun.equals("WITHDRAW")) {
            sql += "join fetch t.withdrawAccount wa "; // n+1 문제를 방지하기 위해 fetch 다시 붙여주기
            sql += "where t.withdrawAccount.id = :withdrawAccountId";
        } else if (gubun.equals("DEPOSIT")) {
            sql += "join fetch t.depositAccount da ";
            sql += "where t.depositAccount.id = :depositAccountId";
        } else { // gubun == ALL
            sql += "left join fetch t.withdrawAccount wa ";
            sql += "left join fetch t.depositAccount da ";
            sql += "where t.withdrawAccount.id = :withdrawAccountId ";
            sql += "or ";
            sql += "t.depositAccount.id = :depositAccountId";
        }

        TypedQuery<Transaction> query = em.createQuery(sql, Transaction.class);

        if (gubun.equals("WITHDRAW")) {
            query = query.setParameter("withdrawAccountId", accountId);
        } else if (gubun.equals("DEPOSIT")) {
            query = query.setParameter("depositAccountId", accountId);
        } else {
            query = query.setParameter("withdrawAccountId", accountId).setParameter("depositAccountId", accountId);
        }

        query.setFirstResult(page * 5);
        query.setMaxResults(5);
        return query.getResultList();
    }
}
