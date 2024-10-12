package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Document(collection = "perspective")
public class Perspective implements Serializable {

    @Id
    private String id;

    private String name;
    private List<String> descriptions;
    private CreatedBy createdBy;
    private long createdOn;
    private STATUS status;

    @Getter
    @Setter
    public static class CreatedBy implements Serializable {
        private String name;
        private String document;
    }

    public enum STATUS {
        ACTIVE,
        INACTIVE
    }
}
