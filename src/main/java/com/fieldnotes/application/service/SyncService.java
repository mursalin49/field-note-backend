package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.*;
import com.fieldnotes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SyncService {
    private final CustomerRepository customers;
    private final SiteRepository sites;
    private final FieldNoteRepository notes;

    /**
     * Everything that changed for this user since the given cursor, INCLUDING soft-deleted rows
     * (so other devices can remove them). Sites and notes come with their customer/site names via JOIN FETCH.
     * since = epoch restores the full data set after a reinstall.
     */
    @Transactional(readOnly = true)
    public PullResponse pull(Long uid, Instant since) {
        Instant serverTime = Instant.now();
        return new PullResponse(serverTime,
                customers.findByUserIdAndServerModifiedAtGreaterThan(uid, since).stream().map(CustomerDto::from).toList(),
                sites.findByUserIdAndServerModifiedAtGreaterThan(uid, since).stream().map(SiteDto::from).toList(),
                notes.findByUserIdAndServerModifiedAtGreaterThan(uid, since).stream().map(NoteDto::from).toList());
    }
}
