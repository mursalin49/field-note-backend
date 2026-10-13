package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.ProfileResponse;
import com.fieldnotes.application.service.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfilePhotoService service;

    @GetMapping
    public ProfileResponse profile(@AuthenticationPrincipal Long userId) {
        return service.profile(userId);
    }

    @PostMapping(path = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse upload(@AuthenticationPrincipal Long userId,
                                  @RequestParam("file") MultipartFile file) throws IOException {
        return service.upload(userId, file);
    }

    @DeleteMapping("/photo")
    public ProfileResponse remove(@AuthenticationPrincipal Long userId) throws IOException {
        return service.remove(userId);
    }

    @GetMapping("/photo")
    public ResponseEntity<Resource> photo(@AuthenticationPrincipal Long userId) throws IOException {
        Path path = service.photoPath(userId);
        String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
        String contentType = filename.endsWith(".png") ? "image/png"
                : filename.endsWith(".webp") ? "image/webp" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(path));
    }
}
