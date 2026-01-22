package com.moustass_video.kms_service.controller;

import com.moustass_video.kms_service.dto.FileToSignDto;
import com.moustass_video.kms_service.dto.GenerateKeyDto;
import com.moustass_video.kms_service.dto.RevokeKeyRequestDto;
import com.moustass_video.kms_service.dto.VerifySignDto;
import com.moustass_video.kms_service.service.UserKeyManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
public class KeyController {
    private final UserKeyManagementService keyManagementService;

    public KeyController(UserKeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    /**
     * Génère une nouvelle paire de clés pour l'utilisateur
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateKeyPair(@RequestBody GenerateKeyDto request) {
        try {
            keyManagementService.generateKeyPair(request);
            return new ResponseEntity<>("Keys saved", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Liste toutes les clés d'un utilisateur
     */
    @GetMapping("/getkeys")
    public ResponseEntity<?> listKeys(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(keyManagementService.findAllById(userId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Liste toutes les clés valides d'un utilisateur
     */
    @GetMapping("/getvalidekeys")
    public ResponseEntity<?> listValidKeys(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(keyManagementService.getValidUserKeys(userId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Revoquer une clé
     */
    @PostMapping("/revokekey")
    public ResponseEntity<?> revoke(@RequestBody RevokeKeyRequestDto dto) {
        try {
            return new ResponseEntity<>(keyManagementService.revokeKey(dto), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Signe un fichier avec une clé spécifique
     */
    @PostMapping("/sign")
    public ResponseEntity<String> signFile(@RequestBody FileToSignDto dto){
        try {
            // Signer avec la clé de l'utilisateur
            String signature = keyManagementService.signFileWithUserKey(dto);
            return new ResponseEntity<>(signature, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Vérifie une signature de fichier
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifySignature(@RequestBody VerifySignDto dto) {

        try {
            // Vérifier la signature
            boolean isValid = keyManagementService.verifySignature(dto);

            return new ResponseEntity<>(isValid, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    false,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
