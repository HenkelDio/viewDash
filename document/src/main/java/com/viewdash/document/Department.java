package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "department")
public class Department {

    @Id
    private String id;

    private String label;
    private String name;
    private String status;
    private String emailManager;
}
