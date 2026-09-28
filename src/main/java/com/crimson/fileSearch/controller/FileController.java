package com.crimson.fileSearch.controller;

import com.crimson.fileSearch.dto.request.RenameFileRequest;
import com.crimson.fileSearch.dto.response.FileResponse;
import com.crimson.fileSearch.dto.response.PagedResponse;
import com.crimson.fileSearch.security.CurrentUser;
import com.crimson.fileSearch.service.FileService;
import com.google.common.net.HttpHeaders;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            @RequestParam("file")MultipartFile file,
            @RequestParam(value = "folderId", required = false)UUID folderId
            ){
        FileResponse response = fileService.upload(CurrentUser.id(), folderId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}")
    public FileResponse getMetadata(@PathVariable UUID fileId){
        return fileService.getMetadata(CurrentUser.id(), fileId);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> download(@PathVariable UUID fileId, @RequestParam(defaultValue = "false") boolean redirect){
        String url = fileService.downloadUrl(CurrentUser.id(), fileId);
        if(redirect){
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, url).build();
        }

        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable UUID fileId) {
        fileService.delete(CurrentUser.id(), fileId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{fileId}/rename")
    public FileResponse rename(@PathVariable UUID fileId, @Valid @RequestBody RenameFileRequest request) {
        return fileService.rename(CurrentUser.id(), fileId, request.getNewFileName());
    }

    @GetMapping
    public PagedResponse<FileResponse> list(
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String extension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return fileService.list(CurrentUser.id(), folderId, filename, extension, createdAfter, createdBefore, pageable);
    }


    @GetMapping("/search")
    public PagedResponse<FileResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) UUID folderId,
            @RequestParam(required = false) String extension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return fileService.searchContent(CurrentUser.id(), q, folderId, extension, createdAfter, createdBefore, pageable);
    }


}
