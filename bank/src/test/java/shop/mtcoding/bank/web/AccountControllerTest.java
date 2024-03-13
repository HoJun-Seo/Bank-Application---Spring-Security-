package shop.mtcoding.bank.web;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@Sql("classpath:db/teardown.sql") // sql 쿼리파일 적용
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class AccountControllerTest extends DummyObject {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager em; // PC 관리를 위한 EntityManager 객체 의존성 주입

    @BeforeEach
    public void setUp() throws Exception {
        User user = userRepository.save(newUser("ssar", "쌀"));
        User cos = userRepository.save(newUser("cos", "코스")); // 테스트 픽스쳐 추가
        Account ssarAccount = accountRepository.save(newAccount(1111L, user));
        Account cosAccount = accountRepository.save(newAccount(2222L, cos)); // 테스트 픽스쳐 추가
        em.clear(); // PC 초기화
    }

    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION) // DB에서 username = ssar 로
                                                                                      // UserDetails 를 조회해서 세션에 담아주는
                                                                                      // 어노테이션
    @Test
    public void saveAccount_test() throws Exception {
        // given
        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
        accountSaveReqDto.setNumber(9999L);
        accountSaveReqDto.setPassword(1234L);
        String requestBody = om.writeValueAsString(accountSaveReqDto);
        System.out.println("테스트 : " + requestBody);

        // when
        // Jwt 토큰 헤더를 추가하지 않아도 인가 필터를 통과할 수는 있지만(chain.doFilter) 결국 시큐리티단에서 세션 값 검증에
        // 실패하게 된다.
        // 테스트를 위해 굳이 Jwt 토큰까지 만들어줄 필요는 없다. @WithUserDetails 어노테이션을 통해 시큐리티
        // 세션만 강제로 만들어주면 된다.
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/s/account").content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);
        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated()); // 응답 상태코드 검증
    }

    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    public void findUserAccount_test() throws Exception {
        // given
        User user = userRepository.findByUsername("ssar").orElseThrow(() -> new CustomApiException("유저를 찾을 수 없습니다."));
        System.out.println("테스트 - username : " + user.getUsername() + ", fullname : " + user.getFullname());
        for (Long i = 1L; i < 3L; i++) {
            AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto();
            accountSaveReqDto.setNumber(1111L + i);
            accountSaveReqDto.setPassword(1234L);
            accountRepository.save(accountSaveReqDto.toEntity(user));
        }
        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/s/account/login-user"));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);
        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    public void deleteAccount_test() throws Exception {
        // given
        Long number = 1111L; // 현재 로그인한 사용자가 가지고 있는 계좌의 계좌번호 정확히 입력

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.delete("/api/s/account/" + number));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // then
        // checkOwner 에서 계좌의 소유자가 일치하지 않을 경우 오류 발생
        // resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());

        // 계좌가 잘 삭제된 것을 확인
        assertThrows(CustomApiException.class,
                () -> accountRepository.findByNumber(number)
                        .orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다.")));
        // Junit 테스트에서 delete 쿼리는 DB 관련으로 가장 마지막에 실행되면 콘솔상에서 확인이 되지 않는다.
    }
}
