package com.viewdash.document.DTO;

import com.viewdash.document.Classification;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerListDTO extends AnswerDTO {
    private Classification classification;
}
