package com.fieldnotes.domain.repository;

import com.fieldnotes.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndUserId(UUID id, Long userId);
    List<Customer> findByUserIdAndDeletedFalseOrderByNameAsc(Long userId);
    List<Customer> findByUserIdAndServerModifiedAtGreaterThan(Long userId, Instant since);
}
