package com.fieldnotes.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "field_notes")
@Getter @Setter
public class FieldNote extends SyncEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(nullable = false)
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    @Column(nullable = false)
    private Instant noteDateTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteStatus status;
    private String photoFilename;
}
