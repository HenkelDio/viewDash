package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class Classification {
    @Id
    private String id;

    private String description;
    private Department department;
    private String status;
    private String color;
}
