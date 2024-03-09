package shop.mtcoding.bank.web;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import shop.mtcoding.bank.config.dummy.DummyObject;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.user.User;
import shop.mtcoding.bank.domain.user.UserRepository;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@Transactional
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

    @BeforeEach
    public void setUp() {
        User user = userRepository.save(newUser("ssar", "쌀"));
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
            accountSaveReqDto.setNumber(1110L + i);
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
}
