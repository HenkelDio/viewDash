package com.viewdash.controller;

import com.viewdash.document.Form;
import com.viewdash.document.User;
import com.viewdash.service.NPSService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("nps")
public class NPSController {

    private final NPSService npsService;

    public NPSController(NPSService npsService) {
        this.npsService = npsService;
    }

    @PostMapping("send-nps")
    public ResponseEntity<?> sendNPS(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User principal) throws Exception {
        return npsService.sendNps(file, principal);
    }

    @GetMapping("get-nps")
    public ResponseEntity<?> getNPS(@AuthenticationPrincipal User principal, @RequestHeader long startDate, @RequestHeader long endDate) {
        return npsService.getNps(principal, startDate, endDate);
    }

    @GetMapping("count-answers")
    public ResponseEntity<?> countAnswers(@RequestHeader long startDate, @RequestHeader long endDate, @RequestHeader(required = false) String departmentId) {
        return npsService.countAnswers(startDate, endDate, departmentId);
    }

    @GetMapping("count-feedback-returns")
    public ResponseEntity<?> countFeedbackReturns() {
        return npsService.countFeedbackReturns();
    }

    @GetMapping("get-answers")
    public ResponseEntity<?> getAnswers(@RequestParam String sortBy, @RequestParam String npsId, @RequestHeader long startDate, @RequestHeader long endDate) {
        return npsService.getAnswers(sortBy, npsId, startDate, endDate);
    }

    @GetMapping("get-score-departments")
    public ResponseEntity<?> getScoreDepartments(@RequestHeader long startDate, @RequestHeader long endDate) {
        return npsService.getScoreDepartments(startDate, endDate);
    }

    @GetMapping("get-all-answers")
    public ResponseEntity<?> getAllAnswers(@RequestHeader long startDate, @RequestHeader long endDate, @RequestHeader(required = false) String departmentId, @RequestParam int pageNumber) {
        return npsService.getAllAnswers(startDate, endDate, departmentId, pageNumber);
    }
}
