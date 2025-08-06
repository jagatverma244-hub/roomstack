package com.roomstack.dao;

import com.roomstack.entity.TiffenSearchPayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchTiffenPaymentRepository extends JpaRepository<TiffenSearchPayments,Long> {
    @Query("SELECT r FROM TiffenSearchPayments r WHERE " +
            "CAST(r.paymentTime AS string) = :keyword OR " +
            "LOWER(r.razorpayOrderId) = LOWER(:keyword) OR " +
            "CAST(r.amount AS string) = :keyword OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.tiffen.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.tiffen.Area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(r.user.id AS string) = :keyword OR " +
            "CAST(r.tiffen.id AS string) = :keyword")
    Page<TiffenSearchPayments> tiffenbookingsearchPayments(@Param("keyword") String keyword, Pageable pageable);
}
