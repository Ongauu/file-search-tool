package com.crimson.fileSearch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFolderRequest {
    @NotBlank(message = "folder name is required")
    @Size(max = 255)
    private String name;
}
