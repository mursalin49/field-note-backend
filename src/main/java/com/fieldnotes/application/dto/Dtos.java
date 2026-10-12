package com.fieldnotes.application.dto;

import com.fieldnotes.domain.model.Customer;
import com.fieldnotes.domain.model.FieldNote;
import com.fieldnotes.domain.model.NoteStatus;
import com.fieldnotes.domain.model.Site;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(@NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, max = 72) String password,
                                  @NotBlank String name) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, Long userId, String name, String email) {}

    public record CustomerDto(UUID id, @NotBlank String name, String contactInfo,
                              @NotNull Instant updatedAt, boolean deleted) {
        public static CustomerDto from(Customer c) {
            return new CustomerDto(c.getId(), c.getName(), c.getContactInfo(), c.getUpdatedAt(), c.isDeleted());
        }
    }

    /** customerName is read-only: resolved with a JOIN, never stored on the site. */
    public record SiteDto(UUID id, @NotNull UUID customerId, String customerName,
                          @NotBlank String name, String address,
                          @NotNull Instant updatedAt, boolean deleted) {
        public static SiteDto from(Site s) {
            return new SiteDto(s.getId(), s.getCustomer().getId(), s.getCustomer().getName(),
                    s.getName(), s.getAddress(), s.getUpdatedAt(), s.isDeleted());
        }
    }

    /** siteName / customerId / customerName are read-only (JOINed from sites and customers). */
    public record NoteDto(UUID id, @NotNull UUID siteId, String siteName,
                          UUID customerId, String customerName,
                          @NotBlank String title, String description,
                          Double latitude, Double longitude,
                          @NotNull Instant noteDateTime, @NotNull NoteStatus status,
                          String photoUrl, @NotNull Instant updatedAt, boolean deleted) {
        public static NoteDto from(FieldNote n) {
            Site s = n.getSite();
            Customer c = s.getCustomer();
            String photo = n.getPhotoFilename() == null ? null : "/api/photos/" + n.getPhotoFilename();
            return new NoteDto(n.getId(), s.getId(), s.getName(), c.getId(), c.getName(),
                    n.getTitle(), n.getDescription(), n.getLatitude(), n.getLongitude(),
                    n.getNoteDateTime(), n.getStatus(), photo, n.getUpdatedAt(), n.isDeleted());
        }
    }

    public record PullResponse(Instant serverTime, List<CustomerDto> customers,
                               List<SiteDto> sites, List<NoteDto> notes) {}
}
