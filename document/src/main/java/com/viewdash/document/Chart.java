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

    private String title;
    private String type;
    private String perspective;
    private String process;
    private String department;
    private String responsible;
    private String periodicity;
    private String objective;
    private String formula;
    private List<String> labels;
    private List<ChartData> chartData;
    private CreatedBy createdBy;
    private long createdOn;
    private STATUS status;
    private String year;
    private String mask;

    @Setter
    @Getter
    public static class ChartData implements Serializable {
        private String label;
        private List<Object> data;
        private String backgroundColor;
    }

    @Getter
    @Setter
    public static class CreatedBy implements Serializable {
        private String name;
        private String document;
    }
}
