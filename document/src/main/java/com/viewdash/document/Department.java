package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "department")
public class Department {
    private String label;
    private String name;
    private String status;
}
