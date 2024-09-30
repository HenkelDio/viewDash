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
        Result result = getResult(toEmail, subject, name, token);

        String htmlContent = templateEngine.process("email-password-template", result.context());

        result.helper().setText(htmlContent, true);

        mailSender.send(result.mimeMessage());
    }

    private Result getResult(String toEmail, String subject, String name, String token) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom("ViewDash");
        helper.setTo(toEmail);
        helper.setSubject(subject);

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("token", token);
        return new Result(mimeMessage, helper, context);
    }

    private record Result(MimeMessage mimeMessage, MimeMessageHelper helper, Context context) {
    }

    public void sendResetPasswordEmail(String toEmail, String subject, String name, String token) throws MessagingException {
        Result result = getResult(toEmail, subject, name, token);

        String htmlContent = templateEngine.process("email-reset-password-template", result.context());

        result.helper().setText(htmlContent, true);

        mailSender.send(result.mimeMessage());
    }
}
