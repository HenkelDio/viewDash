package com.viewdash.service;

import com.viewdash.document.Form;
import com.viewdash.document.Nps;
import com.viewdash.document.PatientNps;
import com.viewdash.document.User;
import com.viewdash.service.repository.FormRepository;
import jakarta.mail.MessagingException;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NPSService extends AbstractService {

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

        logger.info("Sending NPS " + principal.getDocument());
        patients.forEach(document -> {
            try {
                emailService.sendNpsEmail(document);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });


        logger.info("Saving NPS " + principal.getDocument());
        Nps nps = new Nps();
        nps.setSentDate(System.currentTimeMillis());
        nps.setSentBy(principal.getDocument());
        nps.setPatientNpsList(patients);
        mongoTemplate.save(nps);
    }

    public ResponseEntity<?> getNps(User principal) {
        logger.info("Getting NPS " + principal.getDocument());

        try {
            return ResponseEntity.ok(mongoTemplate.find(new Query(), Nps.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error");
        }
    }
}
