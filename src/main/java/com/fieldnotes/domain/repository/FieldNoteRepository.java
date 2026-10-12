package com.fieldnotes.domain.repository;

import com.fieldnotes.domain.model.FieldNote;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldNoteRepository extends JpaRepository<FieldNote, UUID>, JpaSpecificationExecutor<FieldNote> {
    Optional<FieldNote> findByIdAndUserId(UUID id, Long userId);
    boolean existsByPhotoFilenameAndUserId(String photoFilename, Long userId);

    @EntityGraph(attributePaths = {"site", "site.customer"})
    List<FieldNote> findByUserIdAndServerModifiedAtGreaterThan(Long userId, Instant since);

    @Override
    @EntityGraph(attributePaths = {"site", "site.customer"})
    List<FieldNote> findAll(Specification<FieldNote> spec, Sort sort);

    @Modifying(flushAutomatically = true)
    @Query("update FieldNote n set n.deleted = true, n.serverModifiedAt = :now " +
           "where n.deleted = false and n.site.id = :sid")
    int softDeleteBySite(@Param("sid") UUID siteId, @Param("now") Instant now);

    @Modifying(flushAutomatically = true)
    @Query("update FieldNote n set n.deleted = true, n.serverModifiedAt = :now " +
           "where n.deleted = false and n.site.id in (select s.id from Site s where s.customer.id = :cid)")
    int softDeleteByCustomer(@Param("cid") UUID customerId, @Param("now") Instant now);
}
