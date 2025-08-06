package com.roomstack.dao;

import com.roomstack.entity.Roompayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PamentRepository extends JpaRepository<Roompayments, Long> {

    @Query("SELECT r FROM Roompayments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.room.location.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.room.location.area) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Roompayments> searchPayments(@Param("keyword") String keyword, Pageable pageable);
}
