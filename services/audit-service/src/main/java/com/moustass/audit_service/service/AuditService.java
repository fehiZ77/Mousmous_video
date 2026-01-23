package com.moustass.audit_service.service;

import com.moustass.audit_service.entity.AuditEvent;
import com.moustass.audit_service.filter.JwtUtils;
import com.moustass.audit_service.utils.SecurityUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AuditService {
    private final JwtUtils jwtUtils;

    private static final String LOG_DIRECTORY = "/logs";
    private static final String LOG_FILE = "/logs/audit.log";   // A refactorer kely fa comique be

    private static final String GENESIS = "0000";
    private static final Path LOG_DIR = Path.of(LOG_DIRECTORY);

    public AuditService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Liste tous les fichiers .log dans /logs (audit.log, audit-corrupted-*.log, etc.)
     */
    public List<Path> listLogFiles() {
        try (Stream<Path> s = Files.list(LOG_DIR)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".log"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public void create(AuditEvent event) {
        String previousHash = getLastHash();
        String date = LocalDateTime.now().toString();

        String token = SecurityUtil.getCurrentToken();
        Map<String, Object> tokenData = jwtUtils.tokenData(token);
        Long userId = (Long)tokenData.get("userId");
        long lineNumber = getNextLineNumber();

        String dataPart = lineNumber
                        + "| " + date
                        + " | userId=" + userId
                        + " | userName=" + tokenData.get("userName")
                        + " | service=" + event.getService()
                        + " | action=" + event.getActionName()
                        + " | details=" + event.getActionDetails()
                        + " | status=" + event.getStatus();

        String currentHash = sha256(previousHash + dataPart);

        String line = dataPart
                + " | prev=" + previousHash
                + " | hash=" + currentHash;

        appendLine(line);
    }

    public int verifyFile(String nameFile) {
        try {
            Path fileToVerify = Path.of(LOG_DIRECTORY + "/" + nameFile);
            if (!Files.exists(fileToVerify)) return -1;

            List<String> lines = Files.readAllLines(fileToVerify);
            String prev = GENESIS;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                String dataPart = line.split("\\| prev=")[0].trim();
                String hash = line.split("hash=")[1];

                String recalculated = sha256(prev + dataPart);

                if (!hash.equals(recalculated)) {
                    if(fileToVerify.getFileName().equals(Path.of(LOG_FILE).getFileName())) rotateIfCorrupted();
                    return i + 1; //
                }

                prev = hash;
            }
            return 0; // fichier OK
        } catch (Exception e) {
            return -1; // erreur technique
        }
    }

    public void rotateIfCorrupted() {
        try {
            Path current = Path.of(LOG_FILE);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            Path corrupted = Path.of(LOG_DIRECTORY + "/audit-corrupted-" + timestamp + ".log");

            Files.move(current, corrupted, StandardCopyOption.REPLACE_EXISTING);

            // créer nouveau fichier
            Files.createFile(current);

        } catch (Exception e) {
            throw new RuntimeException("Cannot rotate corrupted audit log", e);
        }
    }

    public Resource downloadFile(String fileName) throws Exception{
        try {
            Path fileToVerify = Path.of(LOG_DIRECTORY + "/" + fileName);
            if (!Files.exists(fileToVerify)) throw new Exception("No file");
            return new UrlResource(fileToVerify.toUri());
        } catch (Exception e) {
            throw new Exception("File error : " + e.getMessage());
        }
    }

    //  ============= Private function =======================

    private String getLastHash() {
        try {
            Path path = Path.of(LOG_FILE);
            if (!Files.exists(path)) return GENESIS;

            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) return GENESIS;

            String lastLine = lines.get(lines.size() - 1);
            return lastLine.split("hash=")[1];
        } catch (Exception e) {
            return GENESIS;
        }
    }

    private void appendLine(String line) {
        try {
            Files.writeString(
                    Path.of(LOG_FILE),
                    line + "\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            throw new RuntimeException("Cannot write audit log");
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long getNextLineNumber() {
        try {
            Path path = Path.of(LOG_FILE);
            if (!Files.exists(path)) return 1;
            return Files.lines(path).count() + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}
