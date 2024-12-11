package com.viewdash.controller;

import com.viewdash.document.Form;
import com.viewdash.document.User;
import com.viewdash.service.NPSService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> getNPS(@AuthenticationPrincipal User principal) {
        return npsService.getNps(principal);
    }

    @GetMapping("count-answers")
    public ResponseEntity<?> countAnswers() {
        return npsService.countAnswers();
    }

    @GetMapping("count-feedback-returns")
    public ResponseEntity<?> countFeedbackReturns() {
        return npsService.countFeedbackReturns();
    }
}
