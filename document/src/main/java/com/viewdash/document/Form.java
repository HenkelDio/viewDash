package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Document(collection = "form")
public class Form implements Serializable {
    private List<Question> questions;
    private String status;
    private Parameters parameters;


    @Getter
    @Setter
    public static class Question implements Serializable {
        private String index;
        private String title;
        private String inputType;
        private List<String> options;
        private String answer;
        private String observation;
        private Boolean showObservation;
        private List<String> departmentIds;
        private List<String> departmentEmails;
        private boolean required;
    }

    @Getter
    @Setter
    public static class Parameters implements Serializable {
        private boolean feedbackRequest;
        private boolean employeeName;
    }
}
