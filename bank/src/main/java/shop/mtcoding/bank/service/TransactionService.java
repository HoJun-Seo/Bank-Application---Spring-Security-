package shop.mtcoding.bank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.bank.domain.account.Account;
import shop.mtcoding.bank.domain.account.AccountRepository;
import shop.mtcoding.bank.domain.transaction.Transaction;
import shop.mtcoding.bank.domain.transaction.TransactionRepository;
import shop.mtcoding.bank.dto.transaction.TransactionRespDto.TransactionListRespDto;
import shop.mtcoding.bank.handler.ex.CustomApiException;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // 입출금 목록보기
    // 로그인 한 사람만 볼 수 있기 때문에 userId 매개변수 포함
    public TransactionListRespDto viewTransactionList(Long userId, Long accountNumber, String gubun, int page) {

        // 계좌 찾기
        Account accountPS = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));

        // 계좌의 소유자가 맞는지 확인
        accountPS.checkOwner(userId);

        // 입출금 내역 조회
        List<Transaction> transactionListPS = transactionRepository.findTransactionList(accountPS.getId(), gubun, page);

        // 응답 DTO 필요
        return new TransactionListRespDto(accountPS, transactionListPS);
    }
}
