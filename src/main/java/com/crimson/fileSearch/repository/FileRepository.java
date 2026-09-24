package com.crimson.fileSearch.repository;

import com.crimson.fileSearch.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID>, JpaSpecificationExecutor<FileEntity> {
    Optional<FileEntity> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    boolean existsByOwnerIdAndFolderIdAndFilenameAndDeletedAtIsNull(UUID ownerId, UUID folderId, String filename);

    @Query(value = """
            SELECT f.* FROM files f
            WHERE f.user_id = :ownerId
              AND f.deleted_at IS NULL
              AND f.content_tsv @@ websearch_to_tsquery('english', :query)
              AND (:folderId IS NULL OR f.folder_id = :folderId)
              AND (:fileExtension IS NULL OR f.file_extension = :fileExtension)
              AND (:createdAfter IS NULL OR f.created_at >= :createdAfter)
              AND (:createdBefore IS NULL OR f.created_at <= :createdBefore)
            ORDER BY ts_rank(f.content_tsv, websearch_to_tsquery('english', :query)) DESC, f.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM files f
            WHERE f.user_id = :ownerId
              AND f.deleted_at IS NULL
              AND f.content_tsv @@ websearch_to_tsquery('english', :query)
              AND (:folderId IS NULL OR f.folder_id = :folderId)
              AND (:fileExtension IS NULL OR f.file_extension = :fileExtension)
              AND (:createdAfter IS NULL OR f.created_at >= :createdAfter)
              AND (:createdBefore IS NULL OR f.created_at <= :createdBefore)
            """,
            nativeQuery = true)
    Page<FileEntity> fullTextSearch(
            @Param("userId") UUID userId,
            @Param("query") String query,
            @Param("folderId") UUID folderId,
            @Param("fileExtension") String fileExtension,
            @Param("createdAfter") java.time.Instant createdAfter,
            @Param("createdBefore") java.time.Instant createdBefore,
            Pageable pageable);
}
