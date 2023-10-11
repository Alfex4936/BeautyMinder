package app.beautyminder.dto.sms;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class SmsResponseDTO {
    private String requestId;
    private LocalDateTime requestTime;
    private String statusName;
    private String statusCode;

}
