package com.roomstack.dao;

import com.roomstack.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location ,Long> {
}
