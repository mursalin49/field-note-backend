package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.NoteDto;
import com.fieldnotes.domain.model.*;
import com.fieldnotes.domain.repository.*;
import com.fieldnotes.presentation.exception.ApiException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final FieldNoteRepository notes;
    private final SiteRepository sites;
    private final UserRepository users;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** Search + filter. Title/description/site name/customer name are matched via JOINs. */
    @Transactional(readOnly = true)
    public List<NoteDto> search(Long uid, String q, NoteStatus status, UUID siteId) {
        Specification<FieldNote> spec = (root, query, cb) -> {
            Join<FieldNote, Site> site = root.join("site");
            Join<Site, Customer> customer = site.join("customer");
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("user").get("id"), uid));
            p.add(cb.isFalse(root.get("deleted")));
            p.add(cb.isFalse(site.get("deleted")));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (siteId != null) p.add(cb.equal(site.get("id"), siteId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(root.<String>get("title")), like),
                    cb.like(cb.lower(root.<String>get("description")), like),
                    cb.like(cb.lower(site.<String>get("name")), like),
                    cb.like(cb.lower(customer.<String>get("name")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return notes.findAll(spec, Sort.by(Sort.Direction.DESC, "noteDateTime"))
                .stream().map(NoteDto::from).toList();
    }

    @Transactional
    public NoteDto upsert(Long uid, UUID id, NoteDto in) {
        Site site = sites.findByIdAndUserId(in.siteId(), uid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Site not found"));
        FieldNote n = notes.findById(id).orElse(null);
        if (n != null && !n.getUser().getId().equals(uid)) throw new ApiException(HttpStatus.NOT_FOUND, "Note not found");
        if (n == null) {
            n = new FieldNote();
            n.setId(id);
            n.setUser(users.getReferenceById(uid));
        } else if (n.getUpdatedAt().isAfter(in.updatedAt())) {
            return NoteDto.from(n);
        }
        n.setSite(site);
        n.setTitle(in.title().trim());
        n.setDescription(in.description());
        n.setLatitude(in.latitude());
        n.setLongitude(in.longitude());
        n.setNoteDateTime(in.noteDateTime());
        n.setStatus(in.status());
        n.setUpdatedAt(in.updatedAt());
        n.setDeleted(false);
        return NoteDto.from(notes.save(n));          // photo is managed by the photo endpoint only
    }

    @Transactional
    public void delete(Long uid, UUID id) {
        notes.findByIdAndUserId(id, uid).ifPresent(n -> {
            n.setDeleted(true);
            n.setUpdatedAt(Instant.now());
        });
    }

    @Transactional
    public NoteDto uploadPhoto(Long uid, UUID id, MultipartFile file) throws IOException {
        FieldNote n = notes.findByIdAndUserId(id, uid).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        if (file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Empty file");
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "jpg";
        if (!IMAGE_EXT.contains(ext)) throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported image type");

        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);
        String stored = UUID.randomUUID() + "." + ext;
        file.transferTo(dir.resolve(stored));
        if (n.getPhotoFilename() != null) Files.deleteIfExists(dir.resolve(n.getPhotoFilename()));
        n.setPhotoFilename(stored);
        return NoteDto.from(n);
    }

    /** Only the owner of a note may read its photo. */
    @Transactional(readOnly = true)
    public Path photoPath(Long uid, String filename) {
        if (!notes.existsByPhotoFilenameAndUserId(filename, uid)) throw new ApiException(HttpStatus.NOT_FOUND, "Photo not found");
        return Path.of(uploadDir).resolve(filename);
    }
}
