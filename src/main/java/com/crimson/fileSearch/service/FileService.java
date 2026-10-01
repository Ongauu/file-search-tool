package com.crimson.fileSearch.service;

import com.crimson.fileSearch.dto.response.FileResponse;
import com.crimson.fileSearch.dto.response.PagedResponse;
import com.crimson.fileSearch.entity.FileEntity;
import com.crimson.fileSearch.exception.ConflictException;
import com.crimson.fileSearch.exception.ResourceNotFoundException;
import com.crimson.fileSearch.repository.FileRepository;
import com.crimson.fileSearch.repository.FolderRepository;
import com.crimson.fileSearch.specification.FileSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final MinioStorageService storageService;
    private final ContentExtractionService contentExtractionService;

    @Transactional
    public FileResponse upload(Long userId, UUID folderId, MultipartFile multipartFile) {
        requireOwnedFolderIfPresent(userId, folderId);
        String filename = sanitizeFilename(multipartFile.getOriginalFilename());

        if (fileRepository.existsByUserIdAndFolderIdAndFilenameAndDeletedAtIsNull(userId, folderId, filename)) {
            throw new ConflictException("A file named '" + filename + "' already exists in this folder");
        }

        UUID fileId = UUID.randomUUID();
        String objectKey = storageService.buildObjectKey(userId, fileId, filename);

        storageService.upload(objectKey, multipartFile);

        String extractedContent = contentExtractionService.extract(multipartFile);

        FileEntity entity = FileEntity.builder()
                .id(fileId)
                .userId(userId)
                .folderId(folderId)
                .filename(filename)
                .contentType(multipartFile.getContentType())
                .fileExtension(extractExtension(filename))
                .sizeBytes(multipartFile.getSize())
                .storageObjectKey(objectKey)
                .checksumSha256(MinioStorageService.sha256(multipartFile))
                .extractedContent(extractedContent)
                .build();

        fileRepository.save(entity);
        return toResponse(entity);
    }

    public String downloadUrl(Long userId, UUID fileId) {
        FileEntity file = getOwnedFile(userId, fileId);
        return storageService.presignedDownloadUrl(file.getStorageObjectKey(), file.getFilename());
    }

    public InputStream downloadStream(Long userId, UUID fileId) {
        FileEntity file = getOwnedFile(userId, fileId);
        return storageService.download(file.getStorageObjectKey());
    }

    @Transactional
    public void delete(Long userId, UUID fileId) {
        FileEntity file = getOwnedFile(userId, fileId);
        storageService.delete(file.getStorageObjectKey());
        file.setDeletedAt(Instant.now());
        fileRepository.save(file);
    }

    @Transactional
    public FileResponse rename(Long userId, UUID fileId, String newFilename) {
        FileEntity file = getOwnedFile(userId, fileId);
        String sanitized = sanitizeFilename(newFilename);

        if (!sanitized.equalsIgnoreCase(file.getFilename())
                && fileRepository.existsByUserIdAndFolderIdAndFilenameAndDeletedAtIsNull(userId, file.getFolderId(), sanitized)) {
            throw new ConflictException("A file named '" + sanitized + "' already exists in this folder");
        }

        String newObjectKey = storageService.buildObjectKey(userId, fileId, sanitized);
        storageService.rename(file.getStorageObjectKey(), newObjectKey);

        file.setFilename(sanitized);
        file.setFileExtension(extractExtension(sanitized));
        file.setStorageObjectKey(newObjectKey);
        fileRepository.save(file);
        return toResponse(file);
    }

    @Transactional
    public FileResponse move(Long userId, UUID fileId, UUID folderId) {
        requireOwnedFolderIfPresent(userId, folderId);
        FileEntity file = getOwnedFile(userId, fileId);
        if (Objects.equals(file.getFolderId(), folderId)) {
            return toResponse(file);
        }
        if (fileRepository.existsByUserIdAndFolderIdAndFilenameAndDeletedAtIsNull(userId, folderId, file.getFilename())) {
            throw new ConflictException("A file named '" + file.getFilename() + "' already exists in this folder");
        }
        file.setFolderId(folderId);
        fileRepository.save(file);
        return toResponse(file);
    }

    public FileResponse getMetadata(Long userId, UUID fileId) {
        return toResponse(getOwnedFile(userId, fileId));
    }

    public PagedResponse<FileResponse> list(Long userId, UUID folderId, String filenameFragment,
                                            String extension, Instant createdAfter, Instant createdBefore,
                                            Pageable pageable) {
        var spec = FileSpecifications.build(userId, folderId, filenameFragment, extension, createdAfter, createdBefore);
        Page<FileResponse> page = fileRepository.findAll(spec, pageable).map(this::toResponse);
        return PagedResponse.from(page);
    }

    public PagedResponse<FileResponse> searchContent(Long userId, String query, UUID folderId, String extension,
                                                     Instant createdAfter, Instant createdBefore, Pageable pageable) {
        Page<FileEntity> results = fileRepository.fullTextSearch(
                userId, query, folderId, extension, createdAfter, createdBefore, pageable);
        return PagedResponse.from(results.map(this::toResponse));
    }

    private FileEntity getOwnedFile(Long userId, UUID fileId) {
        return fileRepository.findByIdAndUserIdAndDeletedAtIsNull(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
    }

    private void requireOwnedFolderIfPresent(Long userId, UUID folderId) {
        if (folderId == null) {
            return;
        }
        folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("folder not found: " + folderId));
    }

    private String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }
        String cleaned = raw.replaceAll("[/\\\\]", "_").trim();
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500);
        }
        return cleaned;
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1).toLowerCase() : null;
    }

    private FileResponse toResponse(FileEntity f) {
        return FileResponse.builder()
                .id(f.getId())
                .filename(f.getFilename())
                .contentType(f.getContentType())
                .fileExtension(f.getFileExtension())
                .sizeBytes(f.getSizeBytes())
                .folderID(f.getFolderId())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
