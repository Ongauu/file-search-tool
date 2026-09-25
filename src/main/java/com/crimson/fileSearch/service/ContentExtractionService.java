package com.crimson.fileSearch.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ContentExtractionService {

    private final Tika tika = new Tika();

    @Value("${app.content-extraction.enabled}")
    private boolean enabled;

    @Value("${app.content-extraction.max-bytes}")
    private long maxBytes;

    public String extract(MultipartFile file) {
        if (!enabled || file.getSize() > maxBytes) {
            return null;
        }
        try (var is = file.getInputStream()) {
            tika.setMaxStringLength(200_000);
            return tika.parseToString(is);
        } catch (Exception e) {
            log.warn("Content extraction failed for '{}': {}", file.getOriginalFilename(), e.getMessage());
            return null;
        }
    }
}

