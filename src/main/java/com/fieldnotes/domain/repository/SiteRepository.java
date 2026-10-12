package com.fieldnotes.domain.repository;

import com.fieldnotes.domain.model.Site;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
    Optional<Site> findByIdAndUserId(UUID id, Long userId);

    // JOIN FETCH: customer is loaded in the same SQL query (no duplicated customer data, no N+1)
    @Query("select s from Site s join fetch s.customer c " +
           "where s.user.id = :uid and s.deleted = false and c.deleted = false order by s.name")
    List<Site> findActive(@Param("uid") Long uid);

    @Query("select s from Site s join fetch s.customer c " +
           "where s.user.id = :uid and s.deleted = false and c.id = :cid order by s.name")
    List<Site> findActiveByCustomer(@Param("uid") Long uid, @Param("cid") UUID customerId);

    @EntityGraph(attributePaths = "customer")
    List<Site> findByUserIdAndServerModifiedAtGreaterThan(Long userId, Instant since);

    @Modifying(flushAutomatically = true)
    @Query("update Site s set s.deleted = true, s.serverModifiedAt = :now " +
           "where s.customer.id = :cid and s.deleted = false")
    int softDeleteByCustomer(@Param("cid") UUID customerId, @Param("now") Instant now);
}
