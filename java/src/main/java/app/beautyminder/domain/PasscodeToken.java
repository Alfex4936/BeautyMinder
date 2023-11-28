package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "passcode_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PasscodeToken {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Setter
    @Indexed(unique = true)
    private String token;

    @Indexed(unique = true)
    private String email;

    @Setter
    private boolean verified;
    @Setter
    private LocalDateTime expiryDate;

    @Builder
    public PasscodeToken(String email, String token, LocalDateTime expiryDate) {
        this.email = email;
        this.token = token;
        this.expiryDate = expiryDate;
        this.verified = false;
    }
}
