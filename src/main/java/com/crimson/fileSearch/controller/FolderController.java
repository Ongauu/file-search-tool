package com.crimson.fileSearch.controller;

import com.crimson.fileSearch.dto.request.CreateFolderRequest;
import com.crimson.fileSearch.dto.response.FolderResponse;
import com.crimson.fileSearch.security.CurrentUser;
import com.crimson.fileSearch.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @PostMapping
    ResponseEntity<FolderResponse> create(@Valid @RequestBody CreateFolderRequest request){
        FolderResponse response = folderService.create(CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
