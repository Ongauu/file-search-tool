package com.crimson.fileSearch.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MoveFileRequest {
    private UUID folderId;
}
