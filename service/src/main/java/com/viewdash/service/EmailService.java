package com.viewdash.service;

import com.viewdash.document.Answer;
import com.viewdash.document.Department;
import com.viewdash.document.Form;
import com.viewdash.document.PatientNps;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MongoTemplate mongoTemplate;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, MongoTemplate mongoTemplate, MongoTemplate mongoTemplate1) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mongoTemplate = mongoTemplate1;
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

    public void sendEmailToManager(Form.Question question, Answer answer) throws MessagingException {
        Form form = mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class);
        if (form == null) {
            throw new IllegalStateException("No active form found.");
        }

        Form.Question questionFound = findQuestionByIndex(form, question.getIndex());

        if(Objects.nonNull((questionFound.getDepartmentIds()))) {
            List<Department> departments = findDepartmentsByIds(questionFound.getDepartmentIds());

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ViewDash");
            helper.setSubject("NPS Defrator");

            Context context = prepareEmailContext(questionFound, question, answer);

            sendEmailsToDepartments(departments, mimeMessage, helper, context, answer);
        }

    }

    private Form.Question findQuestionByIndex(Form form, String index) {
        return form.getQuestions().stream()
                .filter(item -> item.getIndex().equals(index))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question with index " + index + " not found."));
    }

    private List<Department> findDepartmentsByIds(List<String> departmentIds) {
        return mongoTemplate.find(new Query(Criteria.where("_id").in(departmentIds)), Department.class);
    }

    private Context prepareEmailContext(Form.Question question, Form.Question answerQuestion, Answer answer) {
        Context context = new Context();
        context.setVariable("question", question.getTitle());
        context.setVariable("answer", answerQuestion.getAnswer());
        context.setVariable("observation", answerQuestion.getObservation());

        if (answer.isFeedbackReturn()) {
            context.setVariable("patientName", answer.getPatientName());
            context.setVariable("patientPhone", answer.getPatientPhone());
        }

        return context;
    }

    private void sendEmailsToDepartments(
            List<Department> departments, MimeMessage mimeMessage, MimeMessageHelper helper, Context context, Answer answer
    ) {
        String template = answer.isFeedbackReturn() ? "manager-deflator-template" : "manager-deflator-unknown-template";

        departments.forEach(department -> {
            if(department.getEmailManager() != null) {
                try {
                    helper.setTo(department.getEmailManager());
                    String htmlContent = templateEngine.process(template, context);
                    helper.setText(htmlContent, true);
                    mailSender.send(mimeMessage);
                } catch (MessagingException e) {
                    System.err.println("Failed to send email to " + department.getEmailManager() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    private record Result(MimeMessage mimeMessage, MimeMessageHelper helper, Context context) {
    }

    public void sendResetPasswordEmail(String toEmail, String subject, String name, String token) throws MessagingException {
        Result result = getResult(toEmail, subject, name, token);

        String htmlContent = templateEngine.process("email-reset-password-template", result.context());

        result.helper().setText(htmlContent, true);

        mailSender.send(result.mimeMessage());
    }

    public void sendNpsEmail(PatientNps patient, String npsId) throws MessagingException {
        Result result = getResult(patient.getEmail(), "Clínica Los Angeles | Pesquisa de satisfação", patient.getName(), npsId);

        String htmlContent = templateEngine.process("nps-template-email.html", result.context());

        result.helper().setText(htmlContent, true);

        mailSender.send(result.mimeMessage());
    }
}
