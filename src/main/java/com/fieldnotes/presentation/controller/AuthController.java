package com.fieldnotes.presentation.controller;

import com.fieldnotes.application.dto.Dtos.*;
import com.fieldnotes.application.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerJson(@Valid @RequestBody RegisterRequest r)
            throws IOException {
        return auth.register(r);
    }

    @PostMapping(path = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerWithPhoto(
            @RequestParam("name") @NotBlank String name,
            @RequestParam("email") @NotBlank @Email String email,
            @RequestParam("password") @NotBlank @Size(min = 6, max = 72) String password,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto)
            throws IOException {
        RegisterRequest r = new RegisterRequest(email, password, name);
        return auth.register(r, profilePhoto);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }
}
