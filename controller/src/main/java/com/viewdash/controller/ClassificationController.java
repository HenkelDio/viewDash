package com.viewdash.controller;

import com.viewdash.document.Classification;
import com.viewdash.service.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classification")
public class ClassificationController {

    @Autowired
    private ClassificationService classificationService;

    @GetMapping("list")
    public ResponseEntity<List<Classification>> list(@RequestParam String status) {
        return classificationService.list(status);
    }

    @PostMapping("create")
    public ResponseEntity create(@RequestBody Classification classification) {
        return classificationService.create(classification);
    }

    @PutMapping("update")
    public ResponseEntity update(@RequestBody Classification classification) {
        return classificationService.update(classification);
    }

    @PutMapping("change-status")
    public ResponseEntity changeStatus(@RequestParam String id, @RequestParam String status) {
        return classificationService.changeStatus(id, status);
    }

    @PutMapping("save-answer-classification")
    public ResponseEntity saveAnswerClassification(@RequestBody Map<String, String> body) {
        return classificationService.saveAnswerClassification(body);
    }
}
