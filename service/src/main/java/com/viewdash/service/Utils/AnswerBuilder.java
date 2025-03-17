package com.viewdash.service.utils;

import com.viewdash.document.Answer;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.service.EmailService;
import com.viewdash.service.Utils.Utils;
import jakarta.mail.MessagingException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AnswerBuilder {

    private static final String DATE_OF_ADMISSION_INDEX = "15";
    private static final String ANSWER_TYPE_INDEX = "16";
    private static final String ANSWER_TYPE_SCORE = "13";
    private static final int LOW_NPS_THRESHOLD = 7;

    private final AnswerDTO answerDTO;
    @Getter
    private final Answer answer = new Answer();

    @Getter
    private final List<Form.Question> questions = new ArrayList<>();
    private final String npsId;
    private final EmailService emailService;

    public AnswerBuilder(AnswerDTO answerDTO, String npsId, EmailService emailService) {
        this.answerDTO = answerDTO;
        this.npsId = npsId;
        this.emailService = emailService;
    }

    public void build() throws MessagingException {
        setBasicAnswerDetails();
        processAnswers();
        answer.setQuestions(questions);
    }

    private void setBasicAnswerDetails() {
        answer.setNpsId(npsId);
        answer.setTimestamp(System.currentTimeMillis());
        answer.setFeedbackReturn(answerDTO.getPatientInfo().isPatientFeedbackReturn());
        answer.setPatientName(answerDTO.getPatientInfo().getPatientName());
        answer.setPatientPhone(answerDTO.getPatientInfo().getPatientPhone());
        answer.setPatientEmail(answerDTO.getPatientInfo().getPatientEmail());
    }

    private void processAnswers() {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Melhor controle de threads

        try {
            for (AnswerDTO.Answer item : answerDTO.getAnswers()) {
                Form.Question question = buildQuestion(item);
                handleSpecialIndexes(item);
                questions.add(question);

                if (isLowNpsScore(item)) {
                    sendEmail(executorService, question);
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }

        try {
            if(answer.getPatientEmail() != null && !answer.getPatientEmail().isEmpty()) {
                emailService.sendPatientEmail(answer.getPatientEmail(), answer.getPatientName());
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    private void sendEmail(ExecutorService executorService, Form.Question question) {
        executorService.submit(() -> {
            try {
                sendEmailToManager(question);
            } catch (MessagingException e) {
               e.printStackTrace();
            }
        });
    }


    private Form.Question buildQuestion(AnswerDTO.Answer item) {
        Form.Question question = new Form.Question();
        question.setAnswer(item.getAnswer());
        question.setObservation(item.getObservation());
        question.setIndex(item.getIndex());
        question.setTitle(item.getTitle());
        return question;
    }

    private void handleSpecialIndexes(AnswerDTO.Answer item) {
        String index = item.getIndex();

        if (DATE_OF_ADMISSION_INDEX.equals(index) && item.getAnswer() != null) {
            answer.setDateOfAdmission(convertDateToTimestamp(item.getAnswer()));
        }

        if (ANSWER_TYPE_SCORE.equals(index)) {
            Answer.Score answerScore = new Answer.Score();
            answerScore.setAnswer(item.getAnswer());
            answerScore.setScore(Utils.getScore(item.getAnswer()));
            answer.setScore(answerScore);
        }

        if (ANSWER_TYPE_INDEX.equals(index)) {
            answer.setAnswerType(item.getAnswer());
        }
        if (ANSWER_TYPE_INDEX.equals(index)) {
            answer.setAnswerType(item.getAnswer());
        }
    }

    private boolean isLowNpsScore(AnswerDTO.Answer item) {
        try {
            return Integer.parseInt(item.getAnswer()) < LOW_NPS_THRESHOLD;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendEmailToManager(Form.Question question) throws MessagingException {
        emailService.sendEmailToManager(question, answer);
    }

    private Long convertDateToTimestamp(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
