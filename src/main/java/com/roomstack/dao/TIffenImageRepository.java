package com.roomstack.dao;

import com.roomstack.entity.tiffenimage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TIffenImageRepository extends JpaRepository<tiffenimage,Long> {
    List<tiffenimage> findByTiffenId(Long id);


}

