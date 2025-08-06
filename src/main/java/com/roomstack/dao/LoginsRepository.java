package com.roomstack.dao;

import com.roomstack.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginsRepository extends JpaRepository<Login,Long> {
    Optional<Login> findByUsernameIgnoreCase(String username);

}
