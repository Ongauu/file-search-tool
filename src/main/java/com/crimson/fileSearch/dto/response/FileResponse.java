package com.crimson.fileSearch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private UUID id;
    private String filename;
    private String contentType;
    private String fileExtension;
    private Long sizeBytes;
    private UUID folderID;
    private Instant createdAt;
    private Instant updatedAt;
}
