package app.beautyminder.dto.sms;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SmsRequestDTO {
    private String type;
    private String contentType;
    private String countryCode;
    private String from;
    private String content;
    private List<MessageDTO> messages;

}
