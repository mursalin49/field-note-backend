package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.PullResponse;
import com.fieldnotes.application.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {
    private final SyncService service;

    /** GET /api/sync/pull?since=2026-01-01T00:00:00Z  (omit `since` to download everything) */
    @GetMapping("/pull")
    public PullResponse pull(@AuthenticationPrincipal Long uid, @RequestParam(required = false) Instant since) {
        return service.pull(uid, since == null ? Instant.EPOCH : since);
    }
}
