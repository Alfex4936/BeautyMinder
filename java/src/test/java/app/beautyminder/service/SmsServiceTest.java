package app.beautyminder.service;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.service.auth.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SmsService smsService;

    @Test
    public void testMakeSignature() throws NoSuchAlgorithmException, InvalidKeyException {
        smsService.setSecretKey("secretKey");
        smsService.setAccessKey("accessKey");
        smsService.setServiceId("serviceId");

        Long currentTime = System.currentTimeMillis();
        String signature = smsService.makeSignature(currentTime);
        assertNotNull(signature);
    }

    @Test
    public void testSendSms() throws Exception {
        // here I used reflection as only Sms
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        Field restTemplateField = SmsService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(smsService, mockRestTemplate);

        smsService.setSecretKey("secretKey");
        smsService.setAccessKey("accessKey");
        smsService.setServiceId("serviceId");
        smsService.setPhone("phoneNumber");

        User mockUser = mock(User.class);
        when(mockUser.getPhoneNumber()).thenReturn("123456789");

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken("abcd");

        PasswordResetResponse tUser = mock(PasswordResetResponse.class);
        when(tUser.getToken()).thenReturn(passwordResetToken);
        when(tUser.getUser()).thenReturn(mockUser);

        SmsResponseDTO smsResponseDTO = new SmsResponseDTO();

        when(mockRestTemplate.postForObject(any(URI.class), any(HttpEntity.class), eq(SmsResponseDTO.class)))
                .thenReturn(smsResponseDTO); // Mock response

        SmsResponseDTO response = smsService.sendSms(tUser);
        assertNotNull(response);
    }
}