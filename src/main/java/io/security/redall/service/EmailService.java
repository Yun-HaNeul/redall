package io.security.redall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *  이메일 발송 서비스 (실제 SMTP 발송)
 *  application.properties 의 spring.mail.* 를 사용
 *
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 보내는 사람 주소 (properties의 username 과 동일하게)
    @Value("${spring.mail.username}")
    private String fromEmail;

    /* 임시 비밀번호 안내 메일 */
    public void sendTempPassword(String toEmail, String tempPassword){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[redall] 임시 비밀번호 안내");
        message.setText(
                "안녕하세요. \n\n" +
                "요청하신 임시 비밀번호는 다음과 같습니다.\n\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해 주세요.\n"
        );
        mailSender.send(message);
    }

    /* 이메일 인증 링크 메일 - 이메일 인증 단계에서 사용 */
    public void sendVerificationLink(String toEmail, String securedKey){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[redall] 이메일 인증 안내");
        message.setText(
                "안녕하세요. \n\n" +
                "아래 링크를 클릭하여 이메일 인증을 완료해 주세요.\n\n" +
                "http://localhost:8082/api/auth/verify-email?key=" + securedKey + "\n"
        );

        mailSender.send(message);
    }

}
