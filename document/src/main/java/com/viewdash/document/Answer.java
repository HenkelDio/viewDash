package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "answer")
public class Answer {
    private Long timestamp;
    private List<Form.Question> questions;
    private String patientName;
    private String patientPhone;
    private Long dateOfAdmission;
    private String type;
    private String answerType;
}
