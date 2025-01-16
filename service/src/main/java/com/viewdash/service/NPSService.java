package com.viewdash.service;

import com.viewdash.document.*;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class NPSService extends AbstractService {

    public static final Set<String> INVALID_ANSWERS = Set.of("14", "15", "16");
    public static final String SCORE = "score";
    public static final String DEPARTMENT_ID = "departmentId";
    private final EmailService emailService;

    public NPSService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ResponseEntity<?> sendNps(MultipartFile file, User principal) throws Exception {
        try {
            processCSV(file, principal);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error");
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

        Map<String, Long> totalScore = getTotalScore(query, answersQuery);
        return ResponseEntity.ok(totalScore);
    }


    private Map<String, Long> getTotalScore(Query query, Query answersQuery) {
        List<Chart> answers = mongoTemplate.find(query, Chart.class);
        long totalAnswers = mongoTemplate.count(answersQuery, Answer.class);

        Map<String, Long> result = Map.of(
                "detractors", answers.stream().filter(item -> item.getScore().equals("DETRACTOR")).count(),
                "neutrals", answers.stream().filter(item -> item.getScore().equals("NEUTRAL")).count(),
                "promoters", answers.stream().filter(item -> item.getScore().equals("PROMOTER")).count(),
                "total", totalAnswers
        );

        return result;
    }



    public ResponseEntity<Long> countFeedbackReturns() {
        return ResponseEntity.ok(mongoTemplate.count(new Query(Criteria.where("feedbackReturn").is(true)), Answer.class));
    }

    public void processCSV(MultipartFile file, User principal) throws Exception {
        logger.info("Processing NPS " + principal.getDocument());

        String fileType = file.getContentType();
        if (fileType == null || !fileType.equals("text/csv")) {
            throw new Exception("Invalid file type. Only CSV files are allowed.");
        }

        List<PatientNps> patients = new ArrayList<>();
        Set<String> uniqueEmails = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length != 2) {
                    throw new Exception("Invalid CSV format. Each row must have exactly two columns.");
                }

                String email = columns[0].trim();
                String nome = columns[1].trim();

                if (!uniqueEmails.contains(email)) {
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

        logger.info("Sending NPS " + principal.getDocument());
        patients.forEach(document -> {
            try {
                emailService.sendNpsEmail(document, nps.getId());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });

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

            return ResponseEntity.ok(mongoTemplate.find(query, Answer.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error");
        }
    }

    public ResponseEntity<?> getScoreDepartments(long startDate, long endDate) {
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
            promoters.put(department.getLabel(), mongoTemplate.count(promoterQuery, Chart.class));

            Query detractorQuery = new Query();
            if (dateCriteria != null) {
                detractorQuery.addCriteria(dateCriteria);
            }
            detractorQuery.addCriteria(Criteria.where(DEPARTMENT_ID).is(department.getId()).and(SCORE).is("DETRACTOR"));
            detractors.put(department.getLabel(), mongoTemplate.count(detractorQuery, Chart.class));

            Query neutralQuery = new Query();
            if (dateCriteria != null) {
                neutralQuery.addCriteria(dateCriteria);
            }
            neutralQuery.addCriteria(Criteria.where(DEPARTMENT_ID).is(department.getId()).and(SCORE).is("NEUTRAL"));
            neutrals.put(department.getLabel(), mongoTemplate.count(neutralQuery, Chart.class));
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

        long totalDocuments = mongoTemplate.count(query, Chart.class);

        int skip = (pageNumber - 1) * pageSize; // Cálculo do deslocamento
        query.skip(skip).limit(pageSize);

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        List<Chart> charts = mongoTemplate.find(query, Chart.class);


        long totalPages = (long) Math.ceil((double) totalDocuments / pageSize);

        // Monta o payload
        List<Map<String, Object>> payload = new ArrayList<>();
        for (Chart chart : charts) {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("type", chart.getScore());
            payloadMap.put("question", chart.getQuestionTitle());
            payloadMap.put("timestamp", chart.getTimestamp());

            if (Objects.nonNull(chart.getQuestionObservation())) {
                payloadMap.put("observation", chart.getQuestionObservation());
            }

            Answer answer = mongoTemplate.findById(chart.getAnswerId(), Answer.class);
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

}
