package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.NoteDto;
import com.fieldnotes.application.service.NoteService;
import com.fieldnotes.domain.model.NoteStatus;
import com.fieldnotes.presentation.exception.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService service;

    @GetMapping("/notes")
    public List<NoteDto> search(@AuthenticationPrincipal Long uid,
                                @RequestParam(required = false) String q,
                                @RequestParam(required = false) NoteStatus status,
                                @RequestParam(required = false) UUID siteId) {
        return service.search(uid, q, status, siteId);
    }

    @PutMapping("/notes/{id}")
    public NoteDto upsert(@AuthenticationPrincipal Long uid, @PathVariable UUID id, @Valid @RequestBody NoteDto body) {
        return service.upsert(uid, id, body);
    }

    @DeleteMapping("/notes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long uid, @PathVariable UUID id) { service.delete(uid, id); }

    @PostMapping(path = "/notes/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteDto uploadPhoto(@AuthenticationPrincipal Long uid, @PathVariable UUID id,
                               @RequestParam("file") MultipartFile file) throws IOException {
        return service.uploadPhoto(uid, id, file);
    }

    @GetMapping("/photos/{filename:.+}")
    public ResponseEntity<Resource> photo(@AuthenticationPrincipal Long uid, @PathVariable String filename) {
        Path path = service.photoPath(uid, filename);
        if (!Files.exists(path)) throw new ApiException(HttpStatus.NOT_FOUND, "Photo not found");
        String lower = filename.toLowerCase();
        String type = lower.endsWith(".png") ? "image/png" : lower.endsWith(".webp") ? "image/webp" : "image/jpeg";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type)).body(new FileSystemResource(path));
    }
}
