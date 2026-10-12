package com.fieldnotes.application.service;

import com.fieldnotes.application.dto.Dtos.CustomerDto;
import com.fieldnotes.domain.model.Customer;
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
public class CustomerService {
    private final CustomerRepository customers;
    private final SiteRepository sites;
    private final FieldNoteRepository notes;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<CustomerDto> list(Long uid) {
        return customers.findByUserIdAndDeletedFalseOrderByNameAsc(uid).stream().map(CustomerDto::from).toList();
    }

    /** Idempotent create-or-update. Last write wins based on the client's updatedAt. */
    @Transactional
    public CustomerDto upsert(Long uid, UUID id, CustomerDto in) {
        Customer c = customers.findById(id).orElse(null);
        if (c != null && !c.getUser().getId().equals(uid)) throw new ApiException(HttpStatus.NOT_FOUND, "Customer not found");
        if (c == null) {
            c = new Customer();
            c.setId(id);
            c.setUser(users.getReferenceById(uid));
        } else if (c.getUpdatedAt().isAfter(in.updatedAt())) {
            return CustomerDto.from(c);               // server copy is newer -> keep it
        }
        c.setName(in.name().trim());
        c.setContactInfo(in.contactInfo());
        c.setUpdatedAt(in.updatedAt());
        c.setDeleted(false);
        return CustomerDto.from(customers.save(c));
    }

    /** Soft delete, cascading to the customer's sites and their notes. Idempotent. */
    @Transactional
    public void delete(Long uid, UUID id) {
        Customer c = customers.findByIdAndUserId(id, uid).orElse(null);
        if (c == null) return;
        Instant now = Instant.now();
        notes.softDeleteByCustomer(id, now);
        sites.softDeleteByCustomer(id, now);
        c.setDeleted(true);
        c.setUpdatedAt(now);
    }
}
