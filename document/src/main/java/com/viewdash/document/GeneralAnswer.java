package com.viewdash.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Document(collection = "generalanswer")
public class GeneralAnswer {
    private Long timestamp;
    private boolean feedbackReturn;
    private String type;
    private UserInfo userInfo;
    private List<Map<String, Object>> answers;

    @Getter
    @Setter
    private static class UserInfo {
        private String name;
        private String email;
        private String phone;
    }
}
