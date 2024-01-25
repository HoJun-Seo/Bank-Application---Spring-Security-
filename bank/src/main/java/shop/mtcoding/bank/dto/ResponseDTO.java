package shop.mtcoding.bank.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDTO<T> {
    private final Integer code; // 1은 성공, -1은 실패
    private final String msg;
    private final T data;
}
