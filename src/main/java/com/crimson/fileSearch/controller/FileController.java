package com.crimson.fileSearch.controller;

import com.crimson.fileSearch.dto.request.MoveFileRequest;
import com.crimson.fileSearch.dto.request.RenameFileRequest;
import com.crimson.fileSearch.dto.response.FileResponse;
import com.crimson.fileSearch.dto.response.PagedResponse;
import com.crimson.fileSearch.security.UserDetailsImpl;
import com.crimson.fileSearch.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) UUID folderId) {
        FileResponse response = fileService.upload(user.getId(), folderId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}")
    public FileResponse getMetadata(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID fileId) {
        return fileService.getMetadata(user.getId(), fileId);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> download(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID fileId,
            @RequestParam(defaultValue = "false") boolean redirect) {
        String url = fileService.downloadUrl(user.getId(), fileId);
        if (redirect) {
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, url).build();
        }
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID fileId) {
        fileService.delete(user.getId(), fileId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{fileId}/rename")
    public FileResponse rename(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID fileId,
            @Valid @RequestBody RenameFileRequest request) {
        return fileService.rename(user.getId(), fileId, request.getNewFileName());
    }

    @PatchMapping("/{fileId}/move")
    public FileResponse move(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID fileId,
            @RequestBody MoveFileRequest request) {
        return fileService.move(user.getId(), fileId, request.getFolderId());
    }

    @GetMapping
    public PagedResponse<FileResponse> list(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String extension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return fileService.list(user.getId(), folderId, filename, extension, createdAfter, createdBefore, pageable);
    }

    @GetMapping("/search")
    public PagedResponse<FileResponse> search(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam String q,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) String extension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return fileService.searchContent(user.getId(), q, folderId, extension, createdAfter, createdBefore, pageable);
    }
}
