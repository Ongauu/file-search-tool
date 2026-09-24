package com.crimson.fileSearch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(nullable = false, length = 512)
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_extension", length = 32)
    private String fileExtension;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "storage_object_key", nullable = false, unique = true, length = 1024)
    private String storageObjectKey;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "extracted_content", columnDefinition = "TEXT")
    private String extractedContent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}
