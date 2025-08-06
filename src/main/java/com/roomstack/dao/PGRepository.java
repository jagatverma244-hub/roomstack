package com.roomstack.dao;

import com.roomstack.entity.PGEntity;
import com.roomstack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PGRepository extends JpaRepository<PGEntity ,Long> {
List<PGEntity> findByUser(User user);
    List<PGEntity> findByUserId(Long userId);
    @Query("SELECT r FROM PGEntity r WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.Area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.PGName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "STR(r.TOtalBedsAvailable) LIKE %:keyword% OR " +
            "STR(r.uploadDate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.phonenumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:gender IS NULL OR :gender = '' OR LOWER(r.GenderType) = LOWER(:gender)) " +
            "AND (:roomType IS NULL OR :roomType = '' OR LOWER(r.RoomType) = LOWER(:roomType)) " +
            "AND (:minPrice IS NULL OR r.Monthlyrent >= :minPrice) " +
            "AND (:maxPrice IS NULL OR r.Monthlyrent <= :maxPrice)")
    Page<PGEntity> advancedSearch(
            @Param("keyword") String keyword,
            @Param("gender") String gender,
            @Param("roomType") String roomType,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable);

}
