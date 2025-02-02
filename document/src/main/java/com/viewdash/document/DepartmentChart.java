package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document(collection = "departmentchart")
public class DepartmentChart implements Serializable {

    @Id
    private String id;
    private String answerId;
    private String departmentId;
    private String score;
    private long timestamp;
    private String questionTitle;
    private String questionObservation;
}
