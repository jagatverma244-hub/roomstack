package com.roomstack.dao;

import com.roomstack.entity.TiffenPayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TiffenPaymentRepository extends JpaRepository<TiffenPayments,Long> {
    @Query("SELECT r FROM TiffenPayments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.tiffen.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.tiffen.Area) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<TiffenPayments> TiffensearchPayments(@Param("keyword") String keyword, Pageable pageable);
}

