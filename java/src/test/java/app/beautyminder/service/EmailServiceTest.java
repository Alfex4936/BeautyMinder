package app.beautyminder.service;

import app.beautyminder.service.auth.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender emailSender;
    @Mock
    private SpringTemplateEngine templateEngine;

    @Spy
    @InjectMocks
    private EmailService emailService;


    @Test
    void testSendSimpleMessageWithException() {
        doThrow(new RuntimeException("Mail send failure")).when(emailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.sendSimpleMessage("to@example.com", "Subject", "Text"));

        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendSimpleMessage() {
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        emailService.sendSimpleMessage("to@example.com", "Subject", "Text");

        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendVerificationMailException() throws MessagingException {
        // Mock to throw an exception when MimeMessageHelper is created
        doThrow(new MessagingException("Helper creation failed")).when(emailService).createMimeMessageHelper(any());

        // Assert that a ResponseStatusException is thrown when the method is called
        assertThrows(ResponseStatusException.class, () -> emailService.sendVerificationEmail("to@example.com", "token"));

        // Verify that the emailSender.send method is called with a MimeMessage
        verify(emailService).createMimeMessageHelper(any());
    }

    @Test
    void testSendPWResetMailException() throws MessagingException {
        // Mock to throw an exception when MimeMessageHelper is created
        doThrow(new MessagingException("Helper creation failed")).when(emailService).createMimeMessageHelper(any());

        // Assert that a ResponseStatusException is thrown when the method is called
        assertThrows(ResponseStatusException.class, () -> emailService.sendPasswordResetEmail("to@example.com", "token"));

        // Verify that the emailSender.send method is called with a MimeMessage
        verify(emailService).createMimeMessageHelper(any());
    }
}