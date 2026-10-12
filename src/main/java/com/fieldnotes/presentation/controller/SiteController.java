package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.SiteDto;
import com.fieldnotes.application.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService service;

    @GetMapping
    public List<SiteDto> list(@AuthenticationPrincipal Long uid, @RequestParam(required = false) UUID customerId) {
        return service.list(uid, customerId);
    }

    @PutMapping("/{id}")
    public SiteDto upsert(@AuthenticationPrincipal Long uid, @PathVariable UUID id, @Valid @RequestBody SiteDto body) {
        return service.upsert(uid, id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long uid, @PathVariable UUID id) { service.delete(uid, id); }
}
