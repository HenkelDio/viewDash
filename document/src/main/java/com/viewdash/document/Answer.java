package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Document(collection = "answer")
public class Answer {

    @Id
    private String id;

    private Long timestamp;
    private List<Form.Question> questions;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private Long dateOfAdmission;
    private String type;
    private String answerType;
    private boolean feedbackReturn;
    private String npsId;
    private RequestAnswered requestAnswered;
    private Score score;
    private String origin;

    @Getter
    @Setter
    public static class RequestAnswered implements Serializable {
        private String username;
        private long timestamp;
    }

    @Getter
    @Setter
    public static class Score implements Serializable {
        private String score;
        private String answer;
    }
}
