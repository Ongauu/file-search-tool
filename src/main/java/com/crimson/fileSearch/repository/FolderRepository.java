package com.crimson.fileSearch.repository;

import com.crimson.fileSearch.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    Optional<Folder> findByIdAndUserId(UUID id, Long userId);

    List<Folder> findByUserIdAndParentId(Long userId, UUID parentId);

    boolean existsByUserIdAndParentIdAndName(Long userId, UUID parentId, String name);
}
