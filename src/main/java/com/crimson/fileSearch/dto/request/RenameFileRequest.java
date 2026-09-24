package com.crimson.fileSearch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFileRequest {
    @NotBlank(message = "new filename is required")
    private String newFileName;
}
