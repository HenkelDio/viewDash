package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@Document(collation = "user")
public class User implements Serializable {
    private String name;
    private String email;
    private String password;
    private String role;
    private String username;
}
