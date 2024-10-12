package com.viewdash.document.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PerspectiveDTO {
    private String name;
    private List<String> descriptions;
}
