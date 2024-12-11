package com.viewdash.service.Utils;

import com.viewdash.document.Answer;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnswerBuilder {

    private final List<AnswerDTO> answerDTO;
    @Getter
    private Answer answer = new Answer();
    private final List<Form.Question> questions = new ArrayList<>();
    private String npsId;

    public AnswerBuilder(List<AnswerDTO> answerDTO, String npsId) {
        this.answerDTO = answerDTO;
        this.npsId = npsId;
    }

    public void build() {
        for (AnswerDTO item : answerDTO) {
            Form.Question question = new Form.Question();

            if(item.getAnswer() != null) {
                question.setAnswer(item.getAnswer());
            }

            if(item.getObservation() != null) {
                question.setObservation(item.getObservation());
            }

            question.setIndex(item.getIndex());

            if(item.getPatientName() != null) {
                answer.setPatientName(item.getPatientName());
            }

            if(item.getPatientPhone() != null) {
                answer.setPatientPhone(item.getPatientPhone());
            }

            if(item.getIndex().equals("14")) {
                answer.setDateOfAdmission(convertDateToTimestamp(item.getAnswer()));
            }

            if(item.getIndex().equals("15")) {
                answer.setAnswerType(item.getAnswer());
            }

            if(item.getIndex().equals("16")) {
                answer.setFeedbackReturn(Boolean.parseBoolean(item.getAnswer()));
            }

            questions.add(question);
        }

        answer.setNpsId(npsId);
        answer.setQuestions(questions);
        answer.setTimestamp(System.currentTimeMillis());
    }

    private Long convertDateToTimestamp(String answer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(answer, formatter);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
