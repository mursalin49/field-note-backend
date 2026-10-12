package com.fieldnotes.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter @Setter
public class Customer extends SyncEntity {
    @Column(nullable = false)
    private String name;
    private String contactInfo;
}
