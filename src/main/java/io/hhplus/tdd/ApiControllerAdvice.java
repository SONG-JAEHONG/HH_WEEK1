package io.hhplus.tdd;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 콘솔에 전체 stack trace 출력 (디버깅용)
        e.printStackTrace();

        // 예외 메시지를 응답에도 포함
        return ResponseEntity.status(500)
                .body(new ErrorResponse("500", e.getMessage() != null ? e.getMessage() : "에러가 발생했습니다."));
    }
}

