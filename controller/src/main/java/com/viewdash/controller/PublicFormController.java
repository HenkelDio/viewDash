package com.viewdash.controller;

import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.service.PublicFormService;
import jakarta.mail.MessagingException;
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
    public ResponseEntity<Form> getForm(@RequestParam(value = "type", required = false) String type) {
        return publicFormService.getForm(type);
    }

    @PostMapping("save-answer")
    public ResponseEntity<Form> saveAnswer(@RequestBody AnswerDTO answerDTO, @RequestParam(value = "token", required = false) String npsId) throws MessagingException {
        return publicFormService.saveAnswer(answerDTO, npsId);
    }

}
