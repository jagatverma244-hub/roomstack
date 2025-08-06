package com.roomstack.dao;

import com.roomstack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User ,Long> {
    User findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
//    Optional<User> findByNameIgnoreCase(String username);
@Query("SELECT r FROM User r WHERE " +
        "LOWER(r.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(r.mobile) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "CAST(r.createdAt AS string) LIKE CONCAT('%', :keyword, '%')")
Page<User> searchUser(@Param("keyword") String keyword, Pageable pageable);

}
