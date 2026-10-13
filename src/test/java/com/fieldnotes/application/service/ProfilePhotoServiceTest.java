package com.fieldnotes.application.service;

import com.fieldnotes.domain.model.User;
import com.fieldnotes.domain.repository.UserRepository;
import com.fieldnotes.presentation.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfilePhotoServiceTest {
    @TempDir
    Path uploadDirectory;

    private UserRepository users;
    private ProfilePhotoService service;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        service = new ProfilePhotoService(users);
        ReflectionTestUtils.setField(service, "uploadDir", uploadDirectory.toString());
        user = new User();
        user.setId(17L);
        user.setName("Worker");
        user.setEmail("worker@example.com");
        when(users.findById(17L)).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadStoresImageAndSetsAccountPhotoUrl() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        var profile = service.upload(17L, image);

        assertTrue(profile.profilePhotoUrl().startsWith("/api/profile/photo?v="));
        assertTrue(Files.exists(service.photoPath(17L)));
        verify(users).save(user);
    }

    @Test
    void removeClearsProfileAndDeletesStoredImage() throws Exception {
        Path oldPhoto = uploadDirectory.resolve("old.jpg");
        Files.write(oldPhoto, new byte[]{1});
        user.setProfilePhotoFilename("old.jpg");

        var profile = service.remove(17L);

        assertEquals(null, profile.profilePhotoUrl());
        assertFalse(Files.exists(oldPhoto));
        assertEquals(null, user.getProfilePhotoFilename());
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        MockMultipartFile image = new MockMultipartFile(
                "file", "avatar.svg", "image/svg+xml", new byte[]{1, 2, 3});

        ApiException exception = assertThrows(ApiException.class,
                () -> service.upload(17L, image));

        assertEquals("Only JPG, PNG, or WebP images are supported", exception.getMessage());
    }
}
