package app.beautyminder.dto.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserProfileResponse {
    private String email;
    private String password;
}