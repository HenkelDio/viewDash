package com.viewdash.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document(collection = "user")
public class User implements Serializable {
    private String name;
    private String email;

    @JsonIgnore
    private String password;

    private String role;
    private String document;
    private String department;
}
