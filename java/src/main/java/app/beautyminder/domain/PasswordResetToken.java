package app.beautyminder.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PasswordResetToken {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdAt;

    private String token;
    private String email;
    private LocalDateTime expiryDate;

    @Builder
    public PasswordResetToken(String email, String token, LocalDateTime expiryDate) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
