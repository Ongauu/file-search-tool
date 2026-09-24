package com.crimson.fileSearch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateFolderRequest {
    @NotBlank(message = "folder name is required")
    private String name;

    private UUID parentId;
}
