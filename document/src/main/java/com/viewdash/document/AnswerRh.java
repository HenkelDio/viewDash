package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "answerrh")
public class AnswerRh {
    private Long timestamp;
    private String employeeName;
    private String description;
    private AnswerType type;

    @Getter
    @Setter
    private static class AnswerType {
        private String value;
        private String label;
    }
}
