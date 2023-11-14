package app.beautyminder.service.auth;

import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.MessageDTO;
import app.beautyminder.dto.sms.SmsRequestDTO;
import app.beautyminder.dto.sms.SmsResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {
    private final ObjectMapper objectMapper;
    @Value("${naver.cloud.access-key}")
    private String accessKey;
    @Value("${naver.cloud.secret-key}")
    private String secretKey;
    @Value("${naver.cloud.sms.openai-key}")
    private String serviceId;
    @Value("${naver.cloud.sms.sender-phone}")
    private String phone;
    @Value("${server.address-text}")
    private String server;

    public String makeSignature(Long time) throws NoSuchAlgorithmException, InvalidKeyException {
        var space = " ";
        var newLine = "\n";
        var method = "POST";
        var url = "/sms/v2/services/" + this.serviceId + "/messages";
        var timestamp = time.toString(); // current timestamp (epoch)
        var accessKey = this.accessKey; // access key id (from portal or Sub Account)

        var message = method + space + url + newLine + timestamp + newLine + accessKey;

        var signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        var rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeBase64String(rawHmac);
    }

    public SmsResponseDTO sendSms(PasswordResetResponse tUser) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        Long time = System.currentTimeMillis();

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time));

        var resetUrl = server + "/user/reset-password?token=" + tUser.getToken().getToken();
        var content = tUser.getUser().getEmail() + ") to reset: " + resetUrl;

        var messages = new ArrayList<MessageDTO>();
        var messageDto = MessageDTO.builder().to(tUser.getUser().getPhoneNumber()).content(content).build();
        messages.add(messageDto);

        SmsRequestDTO request = SmsRequestDTO.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(phone)
                .content(messageDto.getContent())
                .messages(messages)
                .build();

        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        return restTemplate.postForObject(new URI("https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages"), httpBody, SmsResponseDTO.class);
    }
}