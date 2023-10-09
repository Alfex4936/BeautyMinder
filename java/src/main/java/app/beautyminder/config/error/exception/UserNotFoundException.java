package app.beautyminder.config.error.exception;


import app.beautyminder.config.error.ErrorCode;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}