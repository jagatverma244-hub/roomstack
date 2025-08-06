package com.roomstack.dao;

import com.roomstack.entity.searchroompaments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRoomPaymentRepository extends JpaRepository<searchroompaments, Long> {
    @Query("SELECT r FROM searchroompaments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.room.location.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.room.location.area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(r.user.id AS string) = :keyword OR " +
            "CAST(r.room.id AS string) = :keyword")
    Page<searchroompaments> bookingsearchPayments(@Param("keyword") String keyword, Pageable pageable);
}
