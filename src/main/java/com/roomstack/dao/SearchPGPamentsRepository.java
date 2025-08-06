package com.roomstack.dao;

import com.roomstack.entity.searchPGpaments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchPGPamentsRepository extends JpaRepository<searchPGpaments,Long> {
    @Query("SELECT r FROM searchPGpaments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.pgEntity.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.pgEntity.Area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(r.user.id AS string) = :keyword OR " +
            "CAST(r.pgEntity.id AS string) = :keyword")
    Page<searchPGpaments> pgbookingsearchPayments(@Param("keyword") String keyword, Pageable pageable);
}
