package com.crimson.fileSearch.repository;

import com.crimson.fileSearch.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByIdAndUserId(UUID id, UUID userId);

    List<Folder> findByUserIdAndParentId(UUID userId, UUID parentId);

    boolean existByUserIdAndParentIdAndName(UUID userId, UUID parentId, String name);
}
