package shop.mtcoding.bank.handler.aop;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import shop.mtcoding.bank.handler.ex.CustomValidationException;

@Aspect
@Component
public class CustomValidationAdvice {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {

    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {

    }

    // @Before 는 특정 메서드가 실행되기 전, @After 는 특정 메서드가 실행된 이후
    // Point Cut 메서드를 실행 시켜주는 어노테이션이다.
    // @Around 는 실행 이전, 이후 두 가지 경우 모두에 Point Cut 메서드를 실행 시켜주는 어노테이션이다.
    // Join Point 의 전,후 제어
    @Around("postMapping() || putMapping()")
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs(); // Join Point 의 매개변수들이 배열에 저장됨
        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;

                // 에러 처리
                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();

                    for (FieldError error : bindingResult.getFieldErrors()) {
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }

                    throw new CustomValidationException("유효성 검사 실패", errorMap);
                }
            }
        }

        return proceedingJoinPoint.proceed();
    }
}

// 유효성 검사는 요청에서 HTTP Body 에 데이터가 담겨져 들어오는 경우에만 수행해주면 된다.
// Get, Post, Delete, Put 중에서 Get, Delete 는 Body 데이터가 존재하지 않는다.
// 하지만 Post 의 경우 어떤 데이터를 받아서 생성하는 요청이기 때문에 Body 데이터가 존재하고
// Put 또한 데이터를 받아서 수정하는 요청이기 때문에 Body 데이터가 존재한다.
