package com.viewdash.document.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnswerDTO {
    private String index;
    private String answer;
    private String title;
    private String observation;
    private String patientName;
    private String patientPhone;
}
