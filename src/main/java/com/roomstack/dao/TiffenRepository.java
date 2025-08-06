package com.roomstack.dao;

import com.roomstack.entity.Tiffen;
import com.roomstack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TiffenRepository extends JpaRepository<Tiffen,Long> {
    @Query("SELECT t FROM Tiffen t WHERE " +
            "(:includes IS NULL OR LOWER(t.includes) LIKE LOWER(CONCAT('%', :includes, '%'))) AND " +
            "(:deliveryAvailable IS NULL OR t.deliveryAvailable = :deliveryAvailable) AND " +
            "(:mealType IS NULL OR LOWER(t.mealType) = LOWER(:mealType)) AND " +
            "(:tiffenType IS NULL OR LOWER(t.tiffentype) = LOWER(:tiffenType)) AND " +
            "(:dayavailable IS NULL OR LOWER(t.dayavailable) = LOWER(:dayavailable)) AND " +
            "(:minPrice IS NULL OR t.pricePerMonth >= :minPrice) AND " +
            "(:maxPrice IS NULL OR t.pricePerMonth <= :maxPrice) AND " +
            "(:searchText IS NULL OR " +
            "  LOWER(t.tiffencentername) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "  LOWER(t.Area) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "  LOWER(t.city) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "  LOWER(t.district) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "  LOWER(t.deeplocation) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "  LOWER(t.areaCovered) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Tiffen> advancedSearch(
            @Param("includes") String includes,
            @Param("deliveryAvailable") Boolean deliveryAvailable,
            @Param("mealType") String mealType,
            @Param("tiffenType") String tiffenType,
            @Param("dayavailable") String dayavailable,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("searchText") String searchText,
            Pageable pageable
    );


    List<Tiffen> findByUser(User user);
    List<Tiffen> findByUserId(Long userId);

}
