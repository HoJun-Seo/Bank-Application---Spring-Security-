package shop.mtcoding.bank.config.dummy;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserEnum;

public class DummyObject {

    // 진짜 유저를 만드는 메서드(Entity 를 실제로 save 할 때 사용)
    protected User newUser(String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");

        // id, 생성일자, 수정일자는 save 메서드가 동작하면 자동으로 User 객체에 생성되므로 제외시킨다.
        return User.builder()
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }

    // 가짜유저를 만드는 메서드(테스트를 위해 가짜, stub 을 만들 때 사용)
    // 실제로 save 메서드를 동작시키는 것이 아니기 때문에
    // id, 생성일자, 수정일자를 직접 할당해준다.
    protected User newMockUser(Long id, String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");

        return User.builder()
                .id(id)
                .username(username)
                .password(encPassword)
                .email(username + "@nate.com")
                .fullname(fullname)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserEnum.CUSTOMER)
                .build();
    }
}
