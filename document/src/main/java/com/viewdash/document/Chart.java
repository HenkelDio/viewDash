package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Document(collection = "chart")
public class Chart implements Serializable {

    @Id
    private String id;
    private String answerId;
    private String departmentId;
    private String score;
    private long timestamp;
    private String questionTitle;
    private String questionObservation;
}
