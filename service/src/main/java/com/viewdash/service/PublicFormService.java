package com.viewdash.service;

import com.viewdash.document.Answer;
import com.viewdash.document.DepartmentChart;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.service.Utils.Utils;
import jakarta.mail.MessagingException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PublicFormService extends AbstractService {

    private final EmailService emailService;
    public static final Set<String> INVALID_ANSWERS = Set.of("13", "14", "15");

    public PublicFormService(EmailService emailService) {
        super();
        this.emailService = emailService;
    }

    public ResponseEntity<Form> getForm(String type) {

        if(type != null && !type.isEmpty()) {
            logger.info("Getting form by type: " + type);
            return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("type").is(type).and("status").is("ACTIVE")), Form.class));
        }


        logger.info("Getting form by type general");
        return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("type").is("general").and("status").is("ACTIVE")), Form.class));

    }

    public ResponseEntity<Form> saveAnswer(AnswerDTO answerDTO, String npsId) throws MessagingException {
        logger.info("Saving form");

        com.viewdash.service.utils.AnswerBuilder answerBuilder = new com.viewdash.service.utils.AnswerBuilder(answerDTO, npsId, emailService);
        answerBuilder.build();
        mongoTemplate.save(answerBuilder.getAnswer());
        saveChartInfo(answerBuilder.getAnswer(), answerBuilder.getQuestions());

        logger.info("Form saved");

        return ResponseEntity.ok(new Form());
    }

    public void saveChartInfo(Answer answer, List<Form.Question> questions) {
        for(Form.Question question : questions) {
            String answerFinal = question.getAnswer();
            if(!INVALID_ANSWERS.contains(question.getAnswer()) && !"N/A".equals(answerFinal)) {

                Form form = mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class);
                if (form == null) {
                    throw new IllegalStateException("No active form found.");
                }

                Form.Question questionFound = findQuestionByIndex(form, question.getIndex());

                if(questionFound.getDepartmentIds() != null && !questionFound.getDepartmentIds().isEmpty()) {
                    for(String departmentId : questionFound.getDepartmentIds()) {
                        DepartmentChart departmentChart = new DepartmentChart();
                        departmentChart.setAnswerId(answer.getId());
                        departmentChart.setDepartmentId(departmentId);
                        departmentChart.setScore(Utils.getScore(question.getAnswer()));
                        departmentChart.setTimestamp(answer.getTimestamp());
                        departmentChart.setQuestionTitle(questionFound.getTitle());
                        departmentChart.setQuestionObservation(question.getObservation());
                        mongoTemplate.save(departmentChart);
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
}
