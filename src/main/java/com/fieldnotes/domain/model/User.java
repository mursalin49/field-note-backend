package com.fieldnotes.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String name;
    @Column(name = "profile_photo_filename", length = 100)
    private String profilePhotoFilename;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
