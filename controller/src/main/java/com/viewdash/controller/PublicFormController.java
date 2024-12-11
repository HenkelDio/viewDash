package com.viewdash.controller;

import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.service.PublicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("public/form")
public class PublicFormController {

    @Autowired
    PublicFormService publicFormService;

    @GetMapping("get-form")
    public ResponseEntity<Form> getForm() {
        return publicFormService.getForm();
    }

    @PostMapping("save-answer")
    public ResponseEntity<Form> saveForm(@RequestBody List<AnswerDTO> answers, @RequestParam("token") String npsId) {
        return publicFormService.saveForm(answers, npsId);
    }

}
