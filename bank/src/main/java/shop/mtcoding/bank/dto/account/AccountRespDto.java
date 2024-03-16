package shop.mtcoding.bank.dto.account;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.util.CustomDateUtil;

public class AccountRespDto {

    // 계좌등록 요청 처리 후 응답으로 돌려줄 DTO
    @Getter
    @Setter
    public static class AccountSaveRespDto {
        private Long id;
        private Long number;
        private Long balance;

        public AccountSaveRespDto(Account account) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
        }
    }

    @Getter
    @Setter
    public static class AccountListRespDto {
        private String fullname;
        private List<AccountDto> accounts = new ArrayList<>();

        public AccountListRespDto(User user, List<Account> accounts) {
            this.fullname = user.getFullname();
            // this.accounts = accounts.stream().map((account) -> new
            // AccountDto(account)).collect(Collectors.toList());
            this.accounts = accounts.stream().map(AccountDto::new).collect(Collectors.toList());
        }

        /*
         * AccountDto 는 List 에 속해있는 각 원소들의 타입이다.
         * 여기서 AccountDto 에 User 정보를 가져오지 않고 있는것을 확인할 수 있다.
         * 왜냐하면 어차피 특정 사용자 하나의 계좌목록을 가지고 오는것이기 때문에 조회해오는 계좌의 사용자는 모두 동일인물이며,
         * 이 사용자에 대한 정보는 계좌목록 보기 기능을 서비스에서 호출하기 전에 이미 userId 를 통해
         * userRepository.findById 메서드를 호출해서 사용자에 대한 정보를 가지고 있는 상태이기 때문이다.
         * 그렇기 때문에 AccountListRespDto 객체를 만들 때 findByUser_id 메서드의 실행 결과 가져온 계좌목록과,
         * 이 계좌들의 사용자 이름(fullname) 만을 이용해 응답으로 돌려줄 객체를 만들어주는 것이다.
         * 여기서 만약 계좌목록을 불러올 때 각 계좌(AccountDto)를 호출할 때마다 사용자 정보를 LAZY 로딩을 통해 가져오게 된다면
         * 그만큼 데이터베이스 조인 쿼리가 실행되어 처리속도가 그렇지 않을 때보다 더 느려지게 될 것이다. (성능 상 좋지않음)
         */
        @Getter
        @Setter
        public class AccountDto {
            private Long id;
            private Long number;
            private Long balance;

            /*
             * Entity 객체를 DTO 로 옮긴다.
             * Entity 를 컨트롤러로 넘기면 최종적으로 Entity 객체를 응답으로 돌려주게 되는데,
             * 이때 MessageConverter 가 발동하면 모든 필드에 대해 getter 를 실행시켜서
             * 개발자가 원하지 않는 시점에 LAZY 로딩이 발생하게 될 수도 있다.
             * 그렇기 때문에 기능 구현을 위해 개발자가 원하는 정보들에 대해서만 가져와서 응답으로 돌려줄 객체를 만들어주는 것이 좋다.
             */
            public AccountDto(Account account) {
                this.id = account.getId();
                this.number = account.getNumber();
                this.balance = account.getBalance();
            }
        }
    }

    @Getter
    @Setter
    public static class AccountDepositRespDto {
        private Long id; // 계좌 Id
        private Long number; // 계좌번호
        // 입금하는 쪽 입장에서는 자신이 ATM 기기를 통해 입금을 한 이후
        // 입금한 계좌의 잔액을 확인할 필요가 없다.
        // 그렇기 때문에 입금한 계좌의 잔액에 대해서는 표시해주지 않는다.

        // Transaction 객체 자체가 Entity 이기 때문에 객체내부에 Entity 객체가 들어오면 안된다.
        // 그 이유는 AccountListRespDto 내부에 AccountDto 를 만들어줄 때와 같이
        // Entity 객체를 응답으로 넘겨주면 MessageConverter 가 실행될 때
        // 모든 필드에 대해 getter 가 실행되기 때문에 이 경우 원치않는 타이밍에 LAZY 로딩이 발생할 수 있기 때문이다.
        // 다시 말하지만 기능 구현을 위해 필요한 데이터만 가져와서 응답으로 돌려주는 편이 좋다.
        private TransactionDto transactionDto; // 입금 거래내역 객체

        public AccountDepositRespDto(Account account, Transaction transaction) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.transactionDto = new TransactionDto(transaction);
        }

        @Getter
        @Setter
        // 응답으로 돌려줄 거래내역 DTO
        public class TransactionDto {
            private Long id; // 트랜잭션 id 값
            private String gubun;
            private String sender; // 보낸사람(ATM)
            private String receiver; // 받은사람 계좌번호
            private Long amount;

            // 입금 이후 계좌 잔액,이 필드는 클라이언트에게 전달 x -> 서비스단에서 테스트 용도로만 사용
            @JsonIgnore
            private Long depositAccountBalance;

            private String tel; // 보낸사람 전화번호
            private String createdAt; // 입금 날짜

            public TransactionDto(Transaction transaction) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.amount = transaction.getAmount();
                this.depositAccountBalance = transaction.getDepositAccountBalance();
                this.tel = transaction.getTel();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
            }
        }
    }
}
