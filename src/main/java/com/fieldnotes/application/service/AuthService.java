package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.*;
import com.fieldnotes.domain.model.User;
import com.fieldnotes.domain.repository.UserRepository;
import com.fieldnotes.infrastructure.security.JwtService;
import com.fieldnotes.presentation.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final ProfilePhotoService profilePhotos;

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        return register(r, null);
    }

    @Transactional
    public AuthResponse register(RegisterRequest r, MultipartFile profilePhoto) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        User u = new User();
        u.setEmail(email);
        u.setName(r.name().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        u = users.save(u);
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                profilePhotos.upload(u.getId(), profilePhoto);
            } catch (IOException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not save profile photo");
            }
            u = users.findById(u.getId()).orElseThrow();
        }
        return response(u);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return response(u);
    }

    private AuthResponse response(User u) {
        String profilePhotoUrl = u.getProfilePhotoFilename() == null
                ? null
                : "/api/profile/photo?v=" + u.getProfilePhotoFilename();
        return new AuthResponse(jwt.generate(u.getId()), u.getId(), u.getName(), u.getEmail(),
                profilePhotoUrl);
    }
}
