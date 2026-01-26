package com.moustass.audit_service.controller;

import com.moustass.audit_service.entity.AuditEvent;
import com.moustass.audit_service.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("api/audit")
public class AuditController {
    private final AuditService auditService;

    @Value("${audit.file-path}")
    private String LOG_FILE;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody AuditEvent event) {
        try {
            auditService.create(event);
            return new ResponseEntity<>("Audit saved", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public ResponseEntity<Object> listLogs() {
        try {
            List<Path> files = auditService.listLogFiles();

            // On retourne juste les noms (pas les paths complets)
            List<String> fileNames = files.stream()
                    .map(p -> p.getFileName().toString())
                    .toList();

            return ResponseEntity.ok(fileNames);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/download")
    public ResponseEntity<Object> download(@RequestParam String fileToVerify) throws Exception {
        try {
            Resource resource = auditService.downloadFile(fileToVerify);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToVerify + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/verify")
    public ResponseEntity<Object> verify(@RequestParam String idFile) {
        try {
            return new ResponseEntity<>(auditService.verifyFile(idFile), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
