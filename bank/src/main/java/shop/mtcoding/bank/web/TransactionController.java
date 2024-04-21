package shop.mtcoding.bank.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.config.auth.LoginUser;
import shop.mtcoding.bank.dto.ResponseDTO;
import shop.mtcoding.bank.dto.transaction.TransactionRespDto.TransactionListRespDto;
import shop.mtcoding.bank.service.TransactionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    // @RequestParam 에서 defaultValue 속성값은 기본적으로 문자열로 초기화해주어야 한다.
    // x-www-form-urlencoded 방식에서 쿼리 스트링으로 들어오는 모든 데이터들은 문자열이다.
    @GetMapping("/s/account/{number}/transaction")
    public ResponseEntity<?> findTransactionList(@PathVariable Long number,
            @RequestParam(value = "gubun", defaultValue = "ALL") String gubun,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @AuthenticationPrincipal LoginUser loginUser) {

        TransactionListRespDto transactionListRespDto = transactionService
                .viewTransactionList(loginUser.getUser().getId(), number, gubun, page);
        return new ResponseEntity<>(new ResponseDTO<>(1, "입출금 목록보기 성공", transactionListRespDto), HttpStatus.OK);
    }
}
