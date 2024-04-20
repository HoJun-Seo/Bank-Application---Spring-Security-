package shop.mtcoding.bank.dto.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.util.CustomDateUtil;

public class TransactionRespDto {

    @Getter
    @Setter
    public static class TransactionListRespDto {

        private List<TransactionDto> transactions = new ArrayList<>();

        public TransactionListRespDto(Account account, List<Transaction> transactions) {
            this.transactions = transactions.stream()
                    .map((transaction) -> new TransactionDto(transaction, account.getNumber()))
                    .collect(Collectors.toList());
        }

        @Getter
        @Setter
        public class TransactionDto {
            private Long id;
            private String gubun;
            private Long amount;
            private String sender;
            private String receiver;
            private String tel;
            private String createAt;
            private Long balance;

            public TransactionDto(Transaction transaction, Long accountNumber) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.amount = transaction.getAmount();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.createAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
                this.tel = transaction.getTel() == null ? "없음" : transaction.getTel(); // 입금일 경우에만 tel 정보가 저장됨

                if (transaction.getDepositAccount() == null) {
                    this.balance = transaction.getWithdrawAccountBalance();
                } else if (transaction.getWithdrawAccount() == null) {
                    this.balance = transaction.getDepositAccountBalance();
                } else {
                    // 입출금 계좌가 모두 존재할 경우, 서로 다른 두 계좌간의 거래가 발생한 경우

                    // 현재 로그인한 사용자의 계좌와 해당 거래에서의 입금 계좌가 동일한 경우.
                    // 즉, 다른 계좌로부터 사용자의 계좌에 입금 거래가 발생한 경우
                    if (accountNumber.longValue() == transaction.getDepositAccount().getNumber()) {
                        this.balance = transaction.getDepositAccountBalance();
                    }
                    // 사용자의 계좌로부터 다른 계좌에 출금 거래가 발생한 경우
                    else {
                        this.balance = transaction.getWithdrawAccountBalance();
                    }
                }
            }

        }
    }
}
