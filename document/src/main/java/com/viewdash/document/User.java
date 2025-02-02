package com.viewdash.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

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
    private List<Department> departments;
    private String token;
    private STATUS status;
    private Permissions permissions;

    @Getter
    @Setter
    public static class Department implements Serializable {
        private String name;
        private String label;
    }

    @Getter
    @Setter
    public static class Permissions implements Serializable {
        private boolean firstLogin;
        private boolean sendNps;
        private boolean viewAnswers;
        private boolean viewDashboard;
        private boolean viewAndEditUsers;
        private boolean viewAndEditDepartments;
    }

    public enum STATUS {
        ACTIVE, INACTIVE;
    }
}
