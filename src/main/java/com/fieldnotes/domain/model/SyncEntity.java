package com.fieldnotes.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

/** Common columns for every user-owned, syncable record. */
@MappedSuperclass
@Getter @Setter
public abstract class SyncEntity {
    @Id
    private UUID id;                       // generated on the client (works offline)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                     // owner -> data isolation

    @Column(nullable = false)
    private Instant updatedAt;             // client edit time (last-write-wins)

    @Column(nullable = false)
    private Instant serverModifiedAt;      // server write time (pull cursor)

    @Column(nullable = false)
    private boolean deleted;               // soft delete so other devices learn about it

    @PrePersist @PreUpdate
    void touch() { serverModifiedAt = Instant.now(); }
}
