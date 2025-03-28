package com.viewdash.controller;

import com.viewdash.document.Department;
import com.viewdash.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        return departmentService.createDepartment(department);
    }

    @PutMapping
    public ResponseEntity<?> updateDepartment(@RequestBody Department department) {
        return departmentService.updateDepartment(department);
    }

    @GetMapping("find-all")
    public ResponseEntity<?> findAllDepartments(@RequestParam String status) {
        return departmentService.findAllDepartments(status);
    }

    @PutMapping("change-status")
    public ResponseEntity<?> changeStatusDepartment(@RequestParam String status, @RequestHeader String name) {
        return departmentService.changeStatusDepartment(status, name);
    }
}
