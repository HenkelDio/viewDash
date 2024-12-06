package com.viewdash.controller;

import com.viewdash.document.Form;
import com.viewdash.service.PublicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("public/form")
public class PublicFormController {

    @Autowired
    PublicFormService publicFormService;

    @GetMapping("get-form")
    public ResponseEntity<Form> getForm() {
        return publicFormService.getForm();
    }

}
