package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.CustomerDto;
import com.fieldnotes.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @GetMapping
    public List<CustomerDto> list(@AuthenticationPrincipal Long uid) { return service.list(uid); }

    @PutMapping("/{id}")
    public CustomerDto upsert(@AuthenticationPrincipal Long uid, @PathVariable UUID id, @Valid @RequestBody CustomerDto body) {
        return service.upsert(uid, id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long uid, @PathVariable UUID id) { service.delete(uid, id); }
}
