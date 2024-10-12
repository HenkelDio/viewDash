package com.viewdash.controller;

import com.viewdash.document.DTO.PerspectiveDTO;
import com.viewdash.document.Perspective;
import com.viewdash.document.User;
import com.viewdash.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    @Autowired
    ModuleService moduleService;

    @GetMapping("perspective/find-all")
    public ResponseEntity<List<Perspective>> listPerspective(@RequestParam String status) {
        return moduleService.listPerspectives(status);
    }

    @GetMapping("perspective/find-by-id")
    public ResponseEntity<Perspective> findPerspectiveById(@RequestParam String id) {
        return moduleService.findPerspectiveById(id);
    }

    @PostMapping("perspective/create")
    public ResponseEntity<Perspective> createPerspective(@RequestBody PerspectiveDTO perspective, @AuthenticationPrincipal User principal) {
        return moduleService.createPerspective(perspective, principal);
    }

    @PutMapping("change-status")
    public ResponseEntity<?> changeStatusDepartment(@RequestParam String status, @RequestHeader String id, @RequestHeader String type) {
        return moduleService.changeStatus(status, id, type);
    }

}
