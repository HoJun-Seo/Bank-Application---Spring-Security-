package shop.mtcoding.bank.dto.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class UserReqDto {

    @Getter
    @Setter
    public static class JoinReqDto {

        @Pattern(regexp = "", message = "영문/숫자 2~20자 이내로 작성해주세요")
        @NotEmpty // null 이거나 공백일 수 없다.
        private String username;
        @NotEmpty
        private String password;
        @NotEmpty
        private String email;
        @NotEmpty
        private String fullname;

        public User toEntity(BCryptPasswordEncoder passwordEncoder) {
            return User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .fullname(fullname)
                    .role(UserEnum.CUSTOMER)
                    .build();
        }
    }
}
