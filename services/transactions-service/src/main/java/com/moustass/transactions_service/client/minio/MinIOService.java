package com.moustass.transactions_service.client.minio;

import com.moustass.transactions_service.TransactionException.GlobalException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinIOService {
    private final MinioClient minioClient;
    private static final String BUCKET = "videos";

    public MinIOService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() throws GlobalException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(BUCKET).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }
    }

    public String processMinIOStorage(MultipartFile file) throws GlobalException, IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return objectName;
    }

    public Resource getInputStream(String objectName) throws GlobalException{
        try {
            // Récupère le stream depuis MinIO
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName)
                            .build()
            );

            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    public StatObjectResponse getStatObject(String objectName) throws GlobalException{
        try {
            return  minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(BUCKET)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }
}
