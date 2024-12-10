package com.viewdash.service.Utils;

import com.viewdash.document.Answer;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnswerBuilder {

    List<AnswerDTO> answerDTO;
    Answer answer;
    List<Form.Question> questions;

    public AnswerBuilder(List<AnswerDTO> answerDTO) {
        this.answerDTO = answerDTO;
    }

    public void build() {
        Form.Question question = new Form.Question();

        answerDTO.forEach(item -> {
            if(item.getAnswer() != null) {
                question.setAnswer(item.getAnswer());
            }

            if(item.getObservation() != null) {
                question.setObservation(item.getObservation());
            }

            if(item.getPatientName() != null) {
                answer.setPatientName(item.getPatientName());
            }

            if(item.getPatientPhone() != null) {
                answer.setPatientPhone(item.getPatientPhone());
            }

            if(item.getTitle().equals("Data da internação:")) {
                answer.setDateOfAdmission(convertDateToTimestamp(item.getAnswer()));
            }

            questions.add(question);
        });

        answer.setQuestions(questions);
        answer.setTimestamp(System.currentTimeMillis());
    }

    private Long convertDateToTimestamp(String answer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(answer, formatter);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public Answer getAnswer() {
        return answer;
    }
}
