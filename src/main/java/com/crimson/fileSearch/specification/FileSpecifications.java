package com.crimson.fileSearch.specification;

import com.crimson.fileSearch.entity.FileEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class FileSpecifications {

    private FileSpecifications() {
    }

    public static Specification<FileEntity> ownedBy(UUID ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    public static Specification<FileEntity> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<FileEntity> inFolder(UUID folderId) {
        if (folderId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("folderId"), folderId);
    }

    public static Specification<FileEntity> filenameContains(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return null;
        }
        String pattern = "%" + fragment.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("filename")), pattern);
    }

    public static Specification<FileEntity> hasExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("fileExtension"), extension.toLowerCase());
    }

    public static Specification<FileEntity> createdAfter(Instant from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<FileEntity> createdBefore(Instant to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    /** Chains every filter that is non-null, skipping the rest. */
    public static Specification<FileEntity> build(UUID ownerId, UUID folderId, String filenameFragment,
                                                  String extension, Instant createdAfter, Instant createdBefore) {
        Specification<FileEntity> spec = Specification.where(ownedBy(ownerId)).and(notDeleted());
        spec = andIfPresent(spec, inFolder(folderId));
        spec = andIfPresent(spec, filenameContains(filenameFragment));
        spec = andIfPresent(spec, hasExtension(extension));
        spec = andIfPresent(spec, createdAfter(createdAfter));
        spec = andIfPresent(spec, createdBefore(createdBefore));
        return spec;
    }

    private static Specification<FileEntity> andIfPresent(Specification<FileEntity> base, Specification<FileEntity> addition) {
        return addition == null ? base : base.and(addition);
    }
}

