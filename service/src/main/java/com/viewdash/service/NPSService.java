package com.viewdash.service;

import com.viewdash.document.Form;
import com.viewdash.service.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NPSService extends AbstractService {

    public ResponseEntity<?> sendNps(MultipartFile file) throws Exception {
        validateFile(file);
        return ResponseEntity.ok().build();
    }

    public void validateFile(MultipartFile file) throws Exception {
        String fileType = file.getContentType();

//        if ("text/csv".equalsIgnoreCase(fileType)) {
//            processCsv(file);
//        } else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(fileType)) {
//            processExcel(file);
//        } else {
//            throw new IllegalArgumentException("Tipo de arquivo não suportado: " + fileType);
//        }
    }
}
