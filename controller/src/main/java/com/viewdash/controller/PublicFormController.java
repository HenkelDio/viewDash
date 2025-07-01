package com.viewdash.controller;

import com.viewdash.document.AnswerRh;
import com.viewdash.document.DTO.AnswerDTO;
import com.viewdash.document.Form;
import com.viewdash.document.GeneralAnswer;
import com.viewdash.service.PublicFormService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("save-rh-answer")
    public ResponseEntity saveRhAnswer(@RequestBody AnswerRh answerRhDTO) {
        return publicFormService.saveRhAnswer(answerRhDTO);
    }

    @PostMapping("save-general-answer")
    public ResponseEntity saveGeneralAnswer(@RequestBody GeneralAnswer generalAnswer) {
        return publicFormService.saveGeneralAnswer(generalAnswer);
    }
}
