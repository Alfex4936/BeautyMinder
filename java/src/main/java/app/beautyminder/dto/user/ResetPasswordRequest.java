package app.beautyminder.dto.user;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    private final String token;
    private final String password;

    public ResetPasswordRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

}
