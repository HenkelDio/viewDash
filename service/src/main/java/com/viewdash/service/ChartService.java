package com.viewdash.service;

import com.viewdash.document.Chart;
import com.viewdash.document.Department;
import com.viewdash.document.STATUS;
import com.viewdash.document.User;
import com.viewdash.service.repository.ChartRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChartService extends AbstractService {

    @Autowired
    ChartRepository chartRepository;

    public ResponseEntity<?> createChart(User principal, Chart chart) {
        logger.info("Creating new chart, user: " + principal.getName());
        chart.setCreatedOn(System.currentTimeMillis());
        chart.setStatus(STATUS.ACTIVE);

        Chart.CreatedBy createdBy = new Chart.CreatedBy();
        createdBy.setDocument(principal.getDocument());
        createdBy.setName(principal.getName());
        chart.setCreatedBy(createdBy);

        try {
            chartRepository.insert(chart);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<List<Chart>> findAllByDepartment(User principal, String status, String department, String perspective, String process, String responsible, String year) {
        logger.info("Finding all charts by departments, user: " + principal.getDocument());

        try {
            List<String> departments = principal.getDepartments()
                    .stream()
                    .map(item -> item.getLabel())
                    .collect(Collectors.toList());

            Query query = new Query(Criteria.where("status").is(status));

            if(Objects.nonNull(department) && !department.isBlank()) {
                query.addCriteria(Criteria.where("department").is(department));
            } else {
                query.addCriteria(Criteria.where("department").in(departments));
            }

            if(Objects.nonNull(perspective) && !perspective.isBlank()) {
                query.addCriteria(Criteria.where("perspective").is(perspective));
            }

            if(Objects.nonNull(process) && !process.isBlank()) {
                query.addCriteria(Criteria.where("process").is(process));
            }

            if(Objects.nonNull(responsible) && !responsible.isBlank()) {
                query.addCriteria(Criteria.where("responsible").is(responsible));
            }

            if(Objects.nonNull(year) && !year.isBlank()) {
                query.addCriteria(Criteria.where("year").is(year));
            }



            return ResponseEntity.ok(mongoTemplate.find(query, Chart.class));
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<?> changeStatus(String status, String id) {
        logger.info("Update status chart");
        Query query = new Query(Criteria.where("_id").is(id));

        Chart chart = mongoTemplate.findOne(query, Chart.class);
        if(chart == null){
            return ResponseEntity.notFound().build();
        }

        Update update = new Update().set("status",status);
        mongoTemplate.updateFirst(query, update, Chart.class);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Chart> findAllById(User principal, String id) {
        logger.info("Searching chart by id: " + id + " user " + principal.getDocument());

        try {
            Chart chart = mongoTemplate.findById(id, Chart.class);
            return ResponseEntity.ok(chart);
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<?> updateChart(Chart chart, User principal) {
        logger.info("Updating chart " + chart.getTitle() + " user " + principal.getDocument());

        try {
            Query query = new Query(Criteria.where("_id").is(chart.getId()));
            Chart foundChart = mongoTemplate.findOne(query, Chart.class);

            if(foundChart == null){
                return ResponseEntity.notFound().build();
            }

            chartRepository.save(chart);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error creating chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    public ResponseEntity<?> loadXLSChart(MultipartFile file) {
        logger.info("Loading XLS chart from file: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum arquivo enviado.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Chart chart = new Chart();
                chart.setTitle(row.getCell(0).getStringCellValue());
                chart.setType("bar");
                chart.setProcess(row.getCell(1).getStringCellValue());
                chart.setPerspective(row.getCell(2).getStringCellValue());
                chart.setDepartment(row.getCell(3).getStringCellValue());
                chart.setResponsible(row.getCell(4).getStringCellValue());
                chart.setObjective(row.getCell(5).getStringCellValue());
                chart.setFormula(row.getCell(6).getStringCellValue());
                chart.setPeriodicity(row.getCell(7).getStringCellValue());
                chart.setYear("2024");

                List<String> labels = new ArrayList<>();
                labels.add("Janeiro");
                labels.add("Fevereiro");
                labels.add("Março");
                labels.add("Abril");
                labels.add("Maio");
                labels.add("Junho");
                labels.add("Julho");
                labels.add("Agosto");
                labels.add("Setembro");
                labels.add("Outubro");
                labels.add("Novembro");
                labels.add("Dezembro");
                chart.setLabels(labels);

                List<Chart.ChartData> chartDataList = new ArrayList<>();
                Chart.ChartData chartData = new Chart.ChartData();
                chartData.setLabel(row.getCell(8).getStringCellValue());
                chartData.setBackgroundColor("#FF5733");
                List<Object> data = new ArrayList<>();
                for (int j = 9; j <= 20; j++) {
                    data.add(row.getCell(j).getNumericCellValue());
                }
                chartData.setData(data);
                chartDataList.add(chartData);
                chart.setChartData(chartDataList);

                Chart.CreatedBy createdBy = new Chart.CreatedBy();
                createdBy.setName(chart.getResponsible());
                createdBy.setDocument("0");
                chart.setCreatedBy(createdBy);
                chart.setCreatedOn(System.currentTimeMillis());
                chart.setStatus(STATUS.ACTIVE);

                chartRepository.insert(chart);
            }

            return ResponseEntity.ok("Arquivo processado e dados persistidos com sucesso.");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erro ao processar o arquivo.");
        }
    }

}
