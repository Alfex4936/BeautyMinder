package app.beautyminder.config.error.exception;

import app.beautyminder.config.error.ErrorCode;

public class AccessBaseException extends RuntimeException {


    private final ErrorCode errorCode;

    public AccessBaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AccessBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}