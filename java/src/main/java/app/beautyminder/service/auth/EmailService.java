package app.beautyminder.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.username}")

    private String FROM_ADDRESS;

    @Autowired
    private JavaMailSender emailSender;


    public void sendPasswordResetEmail(String to, String token) {
        String subject = "[BeautyMinder] 비밀번호 초기화";
        String resetUrl = "http://localhost:8080" + "/user/reset-password?token=" + token;
        String text = "초기화하려면 다음 링크를 가주세요: " + resetUrl;

        sendSimpleMessage(to, subject, text);
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
//        message.setTo(to);
        message.setTo(FROM_ADDRESS);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}