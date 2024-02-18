package shop.mtcoding.bank.dto.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class UserReqDto {

    @Getter
    @Setter
    public static class JoinReqDto {

        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요")
        @NotEmpty // null 이거나 공백일 수 없다.
        private String username;

        // 패스워드는 최소, 최대 길이만 지정한다
        // @Size 어노테이션은 String 타입의 데이터에만 사용가능하다.
        @Size(min = 4, max = 20)
        @NotEmpty
        private String password;

        @Pattern(regexp = "^[a-zA-Z0-9]{2,10}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$", message = "이메일 형식으로 작성해주세요")
        @NotEmpty
        private String email;

        @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "영문/한글 1~20자 이내로 작성해주세요")
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
