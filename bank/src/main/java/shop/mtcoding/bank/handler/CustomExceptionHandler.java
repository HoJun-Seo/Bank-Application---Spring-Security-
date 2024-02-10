package shop.mtcoding.bank.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import shop.mtcoding.bank.dto.ResponseDTO;
import shop.mtcoding.bank.handler.ex.CustomApiException;
import shop.mtcoding.bank.handler.ex.CustomValidationException;

@RestControllerAdvice
public class CustomExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // CustomApiException 이 발생할경우 해당 메서드를 실행시킴
    @ExceptionHandler(CustomApiException.class)
    public <T> ResponseEntity<?> apiException(CustomApiException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDTO<>(-1, e.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    // CustomValidationException 이 발생할 경우 해당 메서드를 실행시킴
    @ExceptionHandler(CustomValidationException.class)
    public <T> ResponseEntity<?> validationApiException(CustomValidationException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ResponseDTO<>(-1, e.getMessage(), e.getErrorMap()), HttpStatus.BAD_REQUEST);
    }
}
