package app.beautyminder.service.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}")

    private String FROM_ADDRESS;
    @Value("${server.ngrok-text}")
    private String server;

    public MimeMessageHelper createMimeMessageHelper(MimeMessage message) throws MessagingException {
        return new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_NO, StandardCharsets.UTF_8.name());
    }

    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "[BeautyMinder] 이메일 인증";

        // HTML content
        Context context = new Context();
        context.setVariable("token", token);

        String htmlContent = templateEngine.process("passcode", context);

//        String htmlContent = localFileService.readHtmlTemplate("templates/passcode.html");
//        htmlContent = htmlContent.replace("${token}", token);
//        log.info("BEMINDER: content: {}", htmlContent);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = createMimeMessageHelper(message);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set to 'true' to send HTML

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "failed to send an email.");
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "[BeautyMinder] 비밀번호 초기화";

        // HTML content
        String resetUrl = server + "/user/reset-password?token=" + token;
        Context context = new Context();
        context.setVariable("link", resetUrl);

        String htmlContent = templateEngine.process("reset-password-email", context);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = createMimeMessageHelper(message);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set to 'true' to send HTML

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "failed to send an email.");
        }
    }
//
//    @Async
//    public void sendPasswordResetEmail(String to, String token) {
//        String subject = "[BeautyMinder] 비밀번호 초기화";
//        String resetUrl = server + "/user/reset-password?token=" + token;
//        String text = "초기화하려면 다음 링크를 가주세요: " + resetUrl;
//
//        sendSimpleMessage(to, subject, text);
//    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(to);
        message.setTo(FROM_ADDRESS);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}