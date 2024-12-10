package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "nps")
@Getter
@Setter
public class Nps {
    private Long sentDate;
    private String sentBy;
    private List<PatientNps> patientNpsList;
}
