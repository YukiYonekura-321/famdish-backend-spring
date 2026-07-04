package com.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * fal.ai などの一時URL画像をS3に永久保存する (Ruby の UploadGeneratedImageService 相当)。
 */
@Service
public class S3UploadService {

    private static final Logger log = LoggerFactory.getLogger(S3UploadService.class);

    private final S3Client s3Client;
    private final RestTemplate restTemplate;

    @Value("${app.aws.s3-bucket:}")
    private String bucket;

    @Value("${app.aws.region:ap-northeast-1}")
    private String region;

    public S3UploadService(S3Client s3Client, RestTemplate restTemplate) {
        this.s3Client = s3Client;
        this.restTemplate = restTemplate;
    }

    /**
     * @param folderName S3上のフォルダ名 (例: "recipes", "suggestions")
     * @param recordId   レコードID
     * @param suffix     ファイル名のsuffix (nullable)
     * @param tmpImageUrl fal.ai等が返す一時画像URL
     * @return S3上の永久URL。失敗時は null。
     */
    public String upload(String folderName, Long recordId, String suffix, String tmpImageUrl) {
        if (tmpImageUrl == null || tmpImageUrl.isBlank()) {
            return null;
        }
        if (bucket != null && tmpImageUrl.contains(bucket + ".s3")) {
            // すでに自前のS3にアップロード済み
            return tmpImageUrl;
        }

        String key = folderName + "/" + recordId + (suffix != null ? suffix : "") + ".png";

        try {
            byte[] imageBytes = restTemplate.getForObject(tmpImageUrl, byte[].class);
            if (imageBytes == null) {
                return null;
            }

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/png")
                            .build(),
                    RequestBody.fromBytes(imageBytes)
            );

            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        } catch (Exception e) {
            log.error("[S3UploadService] Failed to upload image for {}/{}. Error: {}", folderName, recordId, e.getMessage());
            return null;
        }
    }
}
