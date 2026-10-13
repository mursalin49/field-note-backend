package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.ProfileResponse;
import com.fieldnotes.domain.model.User;
import com.fieldnotes.domain.repository.UserRepository;
import com.fieldnotes.presentation.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfilePhotoService {
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository users;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public ProfileResponse profile(Long userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse upload(Long userId, MultipartFile file) throws IOException {
        User user = findUser(userId);
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Empty file");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "Profile photo must be 5 MB or smaller");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String extension = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1)
                : extensionFor(contentType);
        if (!IMAGE_EXTENSIONS.contains(extension) || !IMAGE_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, or WebP images are supported");
        }

        Path directory = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = directory.resolve(filename).normalize();
        if (!destination.startsWith(directory)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }
        file.transferTo(destination);

        String previous = user.getProfilePhotoFilename();
        user.setProfilePhotoFilename(filename);
        users.save(user);
        deletePrevious(directory, previous, filename);
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse remove(Long userId) throws IOException {
        User user = findUser(userId);
        String previous = user.getProfilePhotoFilename();
        user.setProfilePhotoFilename(null);
        users.save(user);
        deletePrevious(Path.of(uploadDir).toAbsolutePath().normalize(), previous, null);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Path photoPath(Long userId) {
        User user = findUser(userId);
        String filename = user.getProfilePhotoFilename();
        if (filename == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Profile photo not found");
        }
        Path directory = Path.of(uploadDir).toAbsolutePath().normalize();
        Path path = directory.resolve(filename).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Profile photo not found");
        }
        return path;
    }

    private User findUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProfileResponse toResponse(User user) {
        String url = user.getProfilePhotoFilename() == null
                ? null
                : "/api/profile/photo?v=" + user.getProfilePhotoFilename();
        return new ProfileResponse(user.getId(), user.getName(), user.getEmail(), url);
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private void deletePrevious(Path directory, String previous, String current) throws IOException {
        if (previous == null || previous.equals(current)) return;
        Path old = directory.resolve(previous).normalize();
        if (old.startsWith(directory)) Files.deleteIfExists(old);
    }
}
