package app.beautyminder.dto.user;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    private String token;
    private String password;

    public ResetPasswordRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

}
