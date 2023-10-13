package app.beautyminder.dto;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetResponse {
    private PasswordResetToken token;
    private User user;
}