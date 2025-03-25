package com.viewdash.service;

import com.viewdash.document.*;
import jakarta.mail.MessagingException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

@Service
public class NPSService extends AbstractService {

    public static final Set<String> INVALID_ANSWERS = Set.of("14", "15", "16");
    public static final String SCORE = "score";
    public static final String DEPARTMENT_ID = "departmentId";
    private final EmailService emailService;

    public NPSService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ResponseEntity<List<String>> sendNps(MultipartFile file, User principal) throws Exception {
        try {
            return ResponseEntity.ok(processXLS(file, principal));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    public ResponseEntity<Map<String, Long>> countAnswers(long startDate, long endDate, String departmentId) {

        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        endDate += twentyFourHoursInMillis;

        Query query = new Query();
        Query answersQuery = new Query();
        if(startDate > 0 && endDate > 0) {
            query.addCriteria(Criteria.where("timestamp").gte(startDate).lte(endDate));
            answersQuery.addCriteria(Criteria.where("timestamp").gte(startDate).lte(endDate));
        }

        if(Objects.nonNull(departmentId) && !departmentId.isEmpty()) {
            query.addCriteria(Criteria.where("departmentId").is(departmentId));
        }

        Map<String, Long> totalScore = Objects.nonNull(departmentId) ? getTotalScoreByDepartment(query, answersQuery) : getTotalScore(query, answersQuery);
        return ResponseEntity.ok(totalScore);
    }


    private Map<String, Long> getTotalScoreByDepartment(Query query, Query answersQuery) {
        List<DepartmentChart> answers = mongoTemplate.find(query, DepartmentChart.class);
//        long totalAnswers = answers.stream()
//                .map(DepartmentChart::getAnswerId)
//                .distinct()
//                .count();

        Map<String, Long> result = Map.of(
                "detractors", answers.stream().filter(item -> item.getScore().equals("DETRACTOR")).count(),
                "neutrals", answers.stream().filter(item -> item.getScore().equals("NEUTRAL")).count(),
                "promoters", answers.stream().filter(item -> item.getScore().equals("PROMOTER")).count()
        );

        return result;
    }


    private Map<String, Long> getTotalScore(Query query, Query answersQuery) {
        List<Answer> answers = mongoTemplate.find(query, Answer.class);
        long totalAnswers = mongoTemplate.count(answersQuery, Answer.class);

        Map<String, Long> result = Map.of(
                "detractors", answers.stream().filter(item -> item.getScore().getScore().equals("DETRACTOR")).count(),
                "neutrals", answers.stream().filter(item -> item.getScore().getScore().equals("NEUTRAL")).count(),
                "promoters", answers.stream().filter(item -> item.getScore().getScore().equals("PROMOTER")).count(),
                "total", totalAnswers
        );

        return result;
    }



    public ResponseEntity<Long> countFeedbackReturns() {
        return ResponseEntity.ok(mongoTemplate.count(new Query(Criteria.where("feedbackReturn").is(true)), Answer.class));
    }


    public List<String> processXLS(MultipartFile file, User principal) throws Exception {
        logger.info("Processing NPS " + principal.getDocument());

        String fileType = file.getContentType();
        if (fileType == null || (!fileType.equals("application/vnd.ms-excel") && !fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
            throw new Exception("Invalid file type. Only Excel files are allowed.");
        }

        List<PatientNps> patients = new ArrayList<>();
        Set<String> uniqueEmails = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = file.getOriginalFilename().endsWith(".xls") ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            boolean isFirstRow = true;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Pular cabeçalho
                }

                Cell emailCell = row.getCell(22, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell nameCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                String email = emailCell.toString().trim();
                String nome = nameCell.toString().trim();

                if (!email.isEmpty() && !uniqueEmails.contains(email)) {
                    uniqueEmails.add(email);
                    PatientNps patientNps = new PatientNps();
                    patientNps.setEmail(email);
                    patientNps.setName(nome);
                    patients.add(patientNps);
                }
            }
        } catch (IOException e) {
            throw new Exception("Error while reading the file: " + e.getMessage(), e);
        }

        logger.info("Saving NPS " + principal.getDocument());

        Nps nps = new Nps();
        nps.setSentDate(System.currentTimeMillis());
        nps.setSentBy(principal.getDocument());
        nps.setPatientNpsList(patients);
        mongoTemplate.save(nps);


        return sendEmails(patients, nps);

    }

    private List<String> sendEmails(List<PatientNps> patients, Nps nps) throws ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Callable<String>> tasks = new ArrayList<>();

        for (PatientNps document : patients) {
            tasks.add(() -> {
                try {
                    emailService.sendNpsEmail(document, nps.getId());
                    return null; // Nenhum erro
                } catch (MessagingException e) {
                    logger.error("Error sending email to: " + document.getEmail(), e);
                    return document.getEmail(); // Retorna o erro
                }
            });
        }

        List<String> errors = new ArrayList<>();
        try {
            List<Future<String>> results = executorService.invokeAll(tasks);
            for (Future<String> result : results) {
                String error = result.get();
                if (error != null) {
                    errors.add(error);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Thread execution interrupted", e);
        } finally {
            executorService.shutdown();
        }

        return errors;
    }


    public ResponseEntity<?> getNps(User principal, long startDate, long endDate) {
        logger.info("Getting NPS {}", principal.getDocument());

        Query query = new Query();
        if(startDate > 0 && endDate > 0) {
            query = new Query(Criteria.where("sentDate").gte(startDate).lte(endDate));
        }

        try {
            return ResponseEntity.ok(mongoTemplate.find(query, Nps.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error");
        }
    }

    public ResponseEntity<?> getAnswers(String sortBy, String npsId, long startDate, long endDate) {
        logger.info("Getting answers");

        try {
            Query query = new Query();

            if(sortBy.equals("request")) {
                query.addCriteria(Criteria.where("feedbackReturn").is(true));
            }

            if(Objects.nonNull(npsId) && !npsId.isEmpty()) {
                query.addCriteria(Criteria.where("npsId").is(npsId));
            }

            if(startDate > 0 && endDate > 0) {
                long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
                endDate += twentyFourHoursInMillis;
                query.addCriteria(Criteria.where("timestamp").gte(startDate).lte(endDate));
            }

            query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

            return ResponseEntity.ok(mongoTemplate.find(query, Answer.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error");
        }
    }

    public ResponseEntity<?> getScoreDepartments(long startDate, long endDate) {
        logger.info("Getting score by departments");

        Criteria dateCriteria = null;
        if (startDate > 0 && endDate > 0) {
            long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
            endDate += twentyFourHoursInMillis;
            dateCriteria = Criteria.where("timestamp").gte(startDate).lte(endDate);
        }

        List<Department> departments = mongoTemplate.find(new Query(Criteria.where("status").is("ACTIVE")), Department.class);

        Map<String, Long> promoters = new HashMap<>();
        Map<String, Long> detractors = new HashMap<>();
        Map<String, Long> neutrals = new HashMap<>();

        for (Department department : departments) {
            Query promoterQuery = new Query();
            if (dateCriteria != null) {
                promoterQuery.addCriteria(dateCriteria);
            }
            promoterQuery.addCriteria(Criteria.where(DEPARTMENT_ID).is(department.getId()).and(SCORE).is("PROMOTER"));
            promoters.put(department.getLabel(), mongoTemplate.count(promoterQuery, DepartmentChart.class));

            Query detractorQuery = new Query();
            if (dateCriteria != null) {
                detractorQuery.addCriteria(dateCriteria);
            }
            detractorQuery.addCriteria(Criteria.where(DEPARTMENT_ID).is(department.getId()).and(SCORE).is("DETRACTOR"));
            detractors.put(department.getLabel(), mongoTemplate.count(detractorQuery, DepartmentChart.class));

            Query neutralQuery = new Query();
            if (dateCriteria != null) {
                neutralQuery.addCriteria(dateCriteria);
            }
            neutralQuery.addCriteria(Criteria.where(DEPARTMENT_ID).is(department.getId()).and(SCORE).is("NEUTRAL"));
            neutrals.put(department.getLabel(), mongoTemplate.count(neutralQuery, DepartmentChart.class));
        }

        Map<String, Map<String, Long>> results = Map.of(
                "detractors", detractors,
                "neutrals", neutrals,
                "promoters", promoters
        );

        return ResponseEntity.ok(results);
    }

    public ResponseEntity<?> getAllAnswers(long startDate, long endDate, String departmentId, int pageNumber) {
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        endDate += twentyFourHoursInMillis;

        int pageSize = 5;

        Query query = new Query();

        if (startDate > 0 && endDate > 0) {
            query.addCriteria(Criteria.where("timestamp").gte(startDate).lte(endDate));
        }

        if (Objects.nonNull(departmentId) && !departmentId.isEmpty()) {
            query.addCriteria(Criteria.where("departmentId").is(departmentId));
        }

        long totalDocuments = mongoTemplate.count(query, DepartmentChart.class);

        int skip = (pageNumber - 1) * pageSize; // Cálculo do deslocamento
        query.skip(skip).limit(pageSize);

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        List<DepartmentChart> departmentCharts = mongoTemplate.find(query, DepartmentChart.class);


        long totalPages = (long) Math.ceil((double) totalDocuments / pageSize);

        // Monta o payload
        List<Map<String, Object>> payload = new ArrayList<>();
        for (DepartmentChart departmentChart : departmentCharts) {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("type", departmentChart.getScore());
            payloadMap.put("question", departmentChart.getQuestionTitle());
            payloadMap.put("timestamp", departmentChart.getTimestamp());

            if (Objects.nonNull(departmentChart.getQuestionObservation())) {
                payloadMap.put("observation", departmentChart.getQuestionObservation());
            }

            Answer answer = mongoTemplate.findById(departmentChart.getAnswerId(), Answer.class);
            if (Objects.nonNull(answer)) {
                payloadMap.put("dateOfAdmission", answer.getDateOfAdmission());

                if (Objects.nonNull(answer.getPatientName()) && !answer.getPatientName().isEmpty()) {
                    payloadMap.put("patientName", answer.getPatientName());
                }
            }

            payload.add(payloadMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", payload);
        response.put("currentPage", pageNumber);
        response.put("totalPages", totalPages);
        response.put("totalDocuments", totalDocuments);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> setRequestAnswered(String answerId, User principal) {
        logger.info("Set request answered {}", answerId);

        try {
            Answer.RequestAnswered requestAnswered = new Answer.RequestAnswered();
            requestAnswered.setTimestamp(System.currentTimeMillis());
            requestAnswered.setUsername(principal.getDocument());

            mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(answerId)),
                    new Update().set("requestAnswered", requestAnswered), Answer.class);

            logger.info("Request answered successfully {}", answerId);
            return ResponseEntity.ok("Request answered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
