package app.beautyminder.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class AddUserRequest {
    @NotNull(message = "Email cannot be null")
    private String email;
    @NotNull(message = "Password cannot be null")
    private String password;

    private String nickname;
    private String profileImage;
    private String phoneNumber;

}