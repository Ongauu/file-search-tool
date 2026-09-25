package com.crimson.fileSearch.service;

import com.crimson.fileSearch.dto.request.CreateFolderRequest;
import com.crimson.fileSearch.dto.response.FolderResponse;
import com.crimson.fileSearch.entity.Folder;
import com.crimson.fileSearch.exception.ConflictException;
import com.crimson.fileSearch.exception.ResourceNotFoundException;
import com.crimson.fileSearch.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;

    @Transactional
    public FolderResponse create(UUID userId, CreateFolderRequest request){
        if(request.getParentId() != null){
            folderRepository.findByIdAndUserId(request.getParentId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("parent folder not found: " + request.getParentId()));
        }

        if(folderRepository.existByUserIdAndParentIdAndName(userId, request.getParentId(), request.getName())){
            throw new ConflictException("a folder named'" + request.getName() + "' already exists");
        }

        Folder folder = Folder.builder()
                .userId(userId)
                .parentId(request.getParentId())
                .name(request.getName())
                .build();
        folderRepository.save(folder);
        return toResponse(folder);
    }

    public List<FolderResponse> list(UUID userId, UUID parentId){
        return folderRepository.findByUserIdAndParentId(userId, parentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FolderResponse get(UUID userId, UUID folderId){
        return folderRepository.findByIdAndUserId(folderId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("folder not found: " + folderId));
    }

    private FolderResponse toResponse(Folder f){
        return FolderResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .parentId(f.getParentId())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
