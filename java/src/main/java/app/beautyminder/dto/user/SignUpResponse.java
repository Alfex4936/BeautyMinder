package app.beautyminder.dto.user;

import app.beautyminder.domain.User;
import lombok.Getter;

@Getter
public class SignUpResponse {

    private final String message;
    private final User user;

    public SignUpResponse(String message, User user) {
        this.message = message;
        this.user = user;
    }

}
