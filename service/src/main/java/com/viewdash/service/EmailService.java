package com.viewdash.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHtmlEmail(String toEmail, String subject, String name, String token) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom("viewdash76@gmail.com");
        helper.setTo(toEmail);
        helper.setSubject(subject);

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("token", token);

        String htmlContent = templateEngine.process("email-password-template", context);

        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}
