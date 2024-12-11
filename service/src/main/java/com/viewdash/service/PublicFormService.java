package com.viewdash.service;

import com.viewdash.document.Answer;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.service.Utils.AnswerBuilder;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PublicFormService extends AbstractService {

    public ResponseEntity<Form> getForm() {
        logger.info("Getting form");
        return ResponseEntity.ok(mongoTemplate.findOne(new Query(Criteria.where("status").is("ACTIVE")), Form.class));
    }

    public ResponseEntity<Form> saveForm(List<AnswerDTO> answerDTO, String npsId) {
        logger.info("Saving form");

        AnswerBuilder answerBuilder = new AnswerBuilder(answerDTO, npsId);
        answerBuilder.build();
        mongoTemplate.save(answerBuilder.getAnswer());

        return ResponseEntity.ok(new Form());
    }

    private String getPatientPhone(List<AnswerDTO> answerDTO) {
        AtomicReference<String> patientPhone = new AtomicReference<>("");

        answerDTO.forEach(answer -> {
            Form.Question question = new Form.Question();

            if(answer.getPatientPhone() != null) {
                patientPhone.set(answer.getPatientPhone());
            }
        });

        return patientPhone.get();
    }

    private List<Form.Question> buildQuestions(List<AnswerDTO> answerDTO) {
        List<Form.Question> questions = new ArrayList<>();

        answerDTO.forEach(answer -> {
            Form.Question question = new Form.Question();

            if(answer.getAnswer() != null) {
                question.setAnswer(answer.getAnswer());
            }

            if(answer.getObservation() != null) {
                question.setObservation(answer.getObservation());
            }

            question.setTitle(answer.getTitle());
            questions.add(question);
        });

        return questions;
    }

}
