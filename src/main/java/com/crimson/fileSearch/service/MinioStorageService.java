package com.crimson.fileSearch.service;

import io.minio.CopyObjectArgs;
import io.minio.SourceObject;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final MinioClient minioClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    @Value("${app.minio.presigned-url-expiry-seconds}")
    private int presignedUrlExpirySeconds;

    public String buildObjectKey(Long userId, UUID fileId, String filename){
        return "%s/%s/%s".formatted(userId, fileId, filename);
    }

    public String upload(String objectKey, MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(is, file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build());
            return objectKey;
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to storage", e);
        }
    }

    public InputStream download (String objectKey){
        try{
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e){
            throw new StorageException("failed to download the file from storage", e);
        }
    }

    public String presignedDownloadUrl(String objectKey, String downloadFilename){
        try{
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(presignedUrlExpirySeconds, TimeUnit.SECONDS)
                    .extraQueryParams(java.util.Map.of(
                            "response-content-disposition",
                            "attachment; filename=\"" + downloadFilename + "\""
                    ))
                    .build());
        } catch (Exception e){
            throw new StorageException("failed to create presigned URL", e);
        }
    }

    public void delete (String objectKey){
        try{
            minioClient.removeObject((RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()));
        } catch(Exception e){
            throw new StorageException("failed to delete file from storage", e);
        }
    }

    public String rename(String oldObjectKey, String newObjectKey) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(newObjectKey)
                    .source(SourceObject.builder().bucket(bucket).object(oldObjectKey).build())
                    .build());
            delete(oldObjectKey);
            return newObjectKey;
        } catch (Exception e) {
            throw new StorageException("Failed to rename file in storage", e);
        }
    }

    public static String sha256(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException("Failed to checksum file", e);
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
