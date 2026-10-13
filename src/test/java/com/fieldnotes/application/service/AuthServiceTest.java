package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.AuthResponse;
import com.fieldnotes.application.dto.Dtos.LoginRequest;
import com.fieldnotes.application.dto.Dtos.RegisterRequest;
import com.fieldnotes.domain.model.User;
import com.fieldnotes.domain.repository.UserRepository;
import com.fieldnotes.infrastructure.security.JwtService;
import com.fieldnotes.presentation.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtService jwt;
    @Mock private ProfilePhotoService profilePhotos;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, encoder, jwt, profilePhotos);
    }

    @Test
    void registerNormalizesEmailAndStoresOnlyEncodedPassword() {
        when(users.existsByEmail("worker@example.com")).thenReturn(false);
        when(encoder.encode("secret1")).thenReturn("encoded-secret");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(jwt.generate(42L)).thenReturn("jwt-token");

        AuthResponse response = service.register(
            new RegisterRequest(" Worker@Example.com ", "secret1", " Field Worker "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertEquals("worker@example.com", captor.getValue().getEmail());
        assertEquals("Field Worker", captor.getValue().getName());
        assertEquals("encoded-secret", captor.getValue().getPasswordHash());
        assertEquals(42L, response.userId());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void registerRejectsDuplicateEmailBeforeSaving() {
        when(users.existsByEmail("worker@example.com")).thenReturn(true);

        ApiException error = assertThrows(ApiException.class,
            () -> service.register(new RegisterRequest("worker@example.com", "secret1", "Worker")));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(users, never()).save(any(User.class));
    }

    @Test
    void loginReturnsJwtForMatchingPassword() {
        User user = new User();
        user.setId(9L);
        user.setEmail("worker@example.com");
        user.setName("Worker");
        user.setPasswordHash("encoded-secret");
        when(users.findByEmail("worker@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("secret1", "encoded-secret")).thenReturn(true);
        when(jwt.generate(9L)).thenReturn("jwt-token");

        AuthResponse response = service.login(new LoginRequest(" Worker@Example.com ", "secret1"));

        assertEquals(9L, response.userId());
        assertEquals("worker@example.com", response.email());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User();
        user.setId(9L);
        user.setEmail("worker@example.com");
        user.setPasswordHash("encoded-secret");
        when(users.findByEmail("worker@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded-secret")).thenReturn(false);

        ApiException error = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("worker@example.com", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
    }
}
