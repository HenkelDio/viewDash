package com.viewdash.document.DTO;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class AnswerDTO {
    private PatientInfo patientInfo;
    private List<Answer> answers;
    private String origin;

    @Getter
    @Setter
    public static class Answer implements Serializable {
        private String index;
        private String answer;
        private String title;
        private String observation;
    }

    @Getter
    @Setter
    public static class PatientInfo implements Serializable {
        private String patientName;
        private String patientPhone;
        private boolean patientFeedbackReturn;
        private String patientEmail;
    }
}
