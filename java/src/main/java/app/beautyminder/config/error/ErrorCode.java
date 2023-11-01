package app.beautyminder.config.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E1", "올바르지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E2", "잘못된 HTTP 메서드를 호출했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E3", "서버 에러가 발생했습니다."),
    ACCESS_DENIED_ERROR(HttpStatus.UNAUTHORIZED, "E4", "올바른 인증이 필요합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E5", "존재하지 않는 엔티티입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E5", "존재하지 않는 유저입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.NOT_FOUND, "E6", "필요한 요청 파라미터가 잘못되었습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "E7", "올바르지 않은 요청 JSON 형식입니다.");


    private final String message;

    private final String code;
    private final HttpStatus status;

    ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}