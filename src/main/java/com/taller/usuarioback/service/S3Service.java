package com.taller.usuarioback.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey,
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket}") String bucketName) {

        log.info("🔐 Configuring S3Client with access key: {}", accessKey);
        log.info("📦 Using bucket: {}", bucketName);
        log.info("🌍 AWS region: {}", region);

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String contentType = file.getContentType();

        log.info("⬆️ Starting upload of file: {}", fileName);
        log.debug("📁 Content type: {}, Size: {}", contentType, file.getSize());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        try {
            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            if (response.sdkHttpResponse().isSuccessful()) {
                String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
                log.info("✅ File uploaded successfully to {}", fileUrl);
                return fileUrl;
            } else {
                log.error("❌ Upload failed. HTTP Status: {}", response.sdkHttpResponse().statusCode());
                throw new RuntimeException("Failed to upload file to S3");
            }

        } catch (Exception e) {
            log.error("🔥 Exception while uploading to S3: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
}
