package shop.mtcoding.bank.dto.account;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.user.User;

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
}
