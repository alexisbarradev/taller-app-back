package com.taller.usuarioback.controller;

// import com.taller.usuarioback.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://3.135.134.201:4200")
public class FileUploadController {

    // private final S3Service s3Service;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    // @Autowired
    // public FileUploadController(S3Service s3Service) {
    //     this.s3Service = s3Service;
    // }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body("Only images (JPEG, PNG, GIF) and PDF files are allowed");
        }

        // File upload temporarily disabled
        return ResponseEntity.status(503).body("File upload is temporarily disabled. S3 service has been removed.");
        
        // String fileUrl = s3Service.uploadFile(file);
        // return ResponseEntity.ok(fileUrl);
    }
} 