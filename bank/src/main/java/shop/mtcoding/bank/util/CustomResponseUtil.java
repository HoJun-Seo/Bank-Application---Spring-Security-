package shop.mtcoding.bank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import shop.mtcoding.bank.dto.ResponseDTO;

public class CustomResponseUtil {

    private final static Logger log = LoggerFactory.getLogger(CustomResponseUtil.class);

    public static <T> void success(HttpServletResponse response, Object dto) {
        try {
            ObjectMapper om = new ObjectMapper();
            ResponseDTO<Object> responseDTO = new ResponseDTO<Object>(1, "로그인 성공", dto);
            String responseBody = om.writeValueAsString(responseDTO);
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(200); // 응답코드 200 Ok
            response.getWriter().println(responseBody);
        } catch (Exception e) {
            log.error("서버 파싱 에러");
        }
    }

    public static <T> void unAuthentication(HttpServletResponse response, String msg) {
        try {
            ObjectMapper om = new ObjectMapper();
            ResponseDTO<?> responseDTO = new ResponseDTO<T>(-1, msg, null);
            String responseBody = om.writeValueAsString(responseDTO);
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(401); // 응답코드 변경 403 -> 401
            response.getWriter().println(responseBody);
        } catch (Exception e) {
            log.error("서버 파싱 에러");
        }
    }
}
