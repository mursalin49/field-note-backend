package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.SiteDto;
import com.fieldnotes.domain.model.Customer;
import com.fieldnotes.domain.model.Site;
import com.fieldnotes.domain.repository.*;
import com.fieldnotes.presentation.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository sites;
    private final CustomerRepository customers;
    private final FieldNoteRepository notes;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<SiteDto> list(Long uid, UUID customerId) {
        List<Site> rows = customerId == null ? sites.findActive(uid) : sites.findActiveByCustomer(uid, customerId);
        return rows.stream().map(SiteDto::from).toList();
    }

    @Transactional
    public SiteDto upsert(Long uid, UUID id, SiteDto in) {
        Customer customer = customers.findByIdAndUserId(in.customerId(), uid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        Site s = sites.findById(id).orElse(null);
        if (s != null && !s.getUser().getId().equals(uid)) throw new ApiException(HttpStatus.NOT_FOUND, "Site not found");
        if (s == null) {
            s = new Site();
            s.setId(id);
            s.setUser(users.getReferenceById(uid));
        } else if (s.getUpdatedAt().isAfter(in.updatedAt())) {
            return SiteDto.from(s);
        }
        s.setCustomer(customer);
        s.setName(in.name().trim());
        s.setAddress(in.address());
        s.setUpdatedAt(in.updatedAt());
        s.setDeleted(false);
        return SiteDto.from(sites.save(s));
    }

    @Transactional
    public void delete(Long uid, UUID id) {
        Site s = sites.findByIdAndUserId(id, uid).orElse(null);
        if (s == null) return;
        Instant now = Instant.now();
        notes.softDeleteBySite(id, now);
        s.setDeleted(true);
        s.setUpdatedAt(now);
    }
}
