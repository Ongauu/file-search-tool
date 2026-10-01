package com.crimson.fileSearch.controller;

import com.crimson.fileSearch.dto.request.CreateFolderRequest;
import com.crimson.fileSearch.dto.request.RenameFolderRequest;
import com.crimson.fileSearch.dto.response.FolderResponse;
import com.crimson.fileSearch.security.UserDetailsImpl;
import com.crimson.fileSearch.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponse> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody CreateFolderRequest request) {
        FolderResponse response = folderService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<FolderResponse> list(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) UUID parentId) {
        return folderService.list(user.getId(), parentId);
    }

    @GetMapping("/{folderId}")
    public FolderResponse get(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID folderId) {
        return folderService.get(user.getId(), folderId);
    }

    @PatchMapping("/{folderId}")
    public FolderResponse rename(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID folderId,
            @Valid @RequestBody RenameFolderRequest request) {
        return folderService.rename(user.getId(), folderId, request.getName());
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID folderId) {
        folderService.delete(user.getId(), folderId);
        return ResponseEntity.noContent().build();
    }
}
