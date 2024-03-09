package shop.mtcoding.bank.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.dto.ResponseDTO;
import shop.mtcoding.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountListRespDto;
import shop.mtcoding.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import shop.mtcoding.bank.service.AccountService;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AccountController {

    private final AccountService accountService;

    // 인증이 필요한 메서드이기 때문에 /s URI 를 매핑시킨다.
    @PostMapping("/s/account")
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountSaveReqDto accountSaveReqDto,
            BindingResult bindingResult, @AuthenticationPrincipal LoginUser loginUser) {

        AccountSaveRespDto accountSaveRespDto = accountService.accountRegister(accountSaveReqDto,
                loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDTO<>(1, "계좌등록 성공", accountSaveRespDto), HttpStatus.CREATED);
    }

    // 인증이 필요하고, account 테이블에 login 한 유저의 계좌만 주세요
    /*
     * API 에 할당해줄 URI 를 설정할 때 어떤 기능이 수행되는건지 명확하게 작성해주어야 한다.
     */
    @GetMapping("/s/account/login-user")
    public ResponseEntity<?> findUserAccount(@AuthenticationPrincipal LoginUser loginUser) {

        AccountListRespDto accountListRespDto = accountService.searchAccountListByUser(loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDTO<>(1, "사용자별_계좌목록 보기 성공", accountListRespDto), HttpStatus.OK);
    }

}
