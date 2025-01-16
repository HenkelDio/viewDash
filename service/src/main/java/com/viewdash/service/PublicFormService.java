package com.viewdash.service;

import com.viewdash.document.Answer;
import com.viewdash.document.Chart;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import jakarta.mail.MessagingException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PublicFormService extends AbstractService {

    private final EmailService emailService;
    public static final Set<String> INVALID_ANSWERS = Set.of("13", "14", "15");

    public PublicFormService(EmailService emailService) {
        super();
        this.emailService = emailService;
    }

    public ResponseEntity<Form> getForm() {
        logger.info("Getting form");
        return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class));
    }

    public ResponseEntity<Form> saveAnswer(AnswerDTO answerDTO, String npsId) throws MessagingException {
        logger.info("Saving form");

        com.viewdash.service.utils.AnswerBuilder answerBuilder = new com.viewdash.service.utils.AnswerBuilder(answerDTO, npsId, emailService);
        answerBuilder.build();
        mongoTemplate.save(answerBuilder.getAnswer());
        saveChartInfo(answerBuilder.getAnswer(), answerBuilder.getQuestions());


        return ResponseEntity.ok(new Form());
    }

    public void saveChartInfo(Answer answer, List<Form.Question> questions) {
        for(Form.Question question : questions) {
            if(!INVALID_ANSWERS.contains(question.getAnswer())) {

                Form form = mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class);
                if (form == null) {
                    throw new IllegalStateException("No active form found.");
                }

                Form.Question questionFound = findQuestionByIndex(form, question.getIndex());

                if(questionFound.getDepartmentIds() != null && !questionFound.getDepartmentIds().isEmpty()) {
                    for(String departmentId : questionFound.getDepartmentIds()) {
                        Chart chart = new Chart();
                        chart.setAnswerId(answer.getId());
                        chart.setDepartmentId(departmentId);
                        chart.setScore(getScore(question.getAnswer()));
                        chart.setTimestamp(answer.getTimestamp());
                        chart.setQuestionTitle(questionFound.getTitle());
                        chart.setQuestionObservation(question.getObservation());
                        mongoTemplate.save(chart);
                    }
                }
            }
        }
    }

    private Form.Question findQuestionByIndex(Form form, String index) {
        return form.getQuestions().stream()
                .filter(item -> item.getIndex().equals(index))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question with index " + index + " not found."));
    }

    private String getScore(String answer) {
        int score = Integer.parseInt(answer);
        if (score <= 6) {
            return "DETRACTOR";
        } else if (score <= 8) {
            return "NEUTRAL";
        }

        return "PROMOTER";
    }


}
