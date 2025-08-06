package com.roomstack.dao;

import com.roomstack.entity.PGPaments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PGpamentsRepository extends JpaRepository<PGPaments,Long> {
    @Query("SELECT r FROM PGPaments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.pgEntity.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.pgEntity.Area) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PGPaments> pgsearchPayments(@Param("keyword") String keyword, Pageable pageable);
}
