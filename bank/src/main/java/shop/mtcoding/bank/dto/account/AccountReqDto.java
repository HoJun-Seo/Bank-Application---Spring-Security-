package shop.mtcoding.bank.dto.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.user.User;

public class AccountReqDto {

    // 계좌등록 요청 시 활용할 DTO
    @Getter
    @Setter
    public static class AccountSaveReqDto {

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long password;

        // 잔액(초기 잔액 1000원)과 user 정보는 받을 필요없음
        // user 정보는 세션에서 가져오면 됨

        public Account toEntity(User user) {
            return Account.builder()
                    .number(number)
                    .password(password)
                    .balance(1000L)
                    .user(user)
                    .build();
        }
    }

    // 계좌입금 요청 시 활용할 DTO
    @Getter
    @Setter
    public static class AccountDepositReqDto {

        @NotNull
        @Digits(integer = 4, fraction = 4)
        private Long number;
        @NotNull
        private Long amount;
        @NotEmpty
        @Pattern(regexp = "^(DEPOSIT)$")
        private String gubun; // DEPOSIT(구분 필드)
        @NotEmpty
        @Pattern(regexp = "^[0-9]{11}")
        private String tel; // 전화번호(누가 입급했는지 확인용)
    }
}
