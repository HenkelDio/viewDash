package com.viewdash.controller;

import com.viewdash.document.Chart;
import com.viewdash.document.User;
import com.viewdash.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/charts")
public class ChartController {

    @Autowired
    ChartService chartService;

    @PostMapping("/create-chart")
    public ResponseEntity<?> createChart(@AuthenticationPrincipal User principal, @RequestBody Chart chart) {
        return chartService.createChart(principal, chart);
    }

    @GetMapping("/find-all-by-department")
    public ResponseEntity<List<Chart>> findAllByDepartment(
            @AuthenticationPrincipal User principal,
            @RequestParam String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String perspective,
            @RequestParam(required = false) String process,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false) String year) {

        return chartService.findAllByDepartment(principal, status, department, perspective, process, responsible, year);
    }

    @GetMapping("/find-by-id")
    public ResponseEntity<Chart> findAllById(@AuthenticationPrincipal User principal, @RequestParam String id) {
        return chartService.findAllById(principal, id);
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeStatusChart(@RequestParam String status, @RequestParam String id) {
        return chartService.changeStatus(status, id);
    }

    @PutMapping("/update-chart")
    public ResponseEntity<?> updateChart(@AuthenticationPrincipal User principal, @RequestBody Chart chart) {
        return chartService.updateChart(chart, principal);
    }

    @PostMapping("/create-charts-xls")
    public ResponseEntity<?> createChartsXls(@RequestBody MultipartFile file) {
        return chartService.loadXLSChart(file);
    }
}
