package com.roomstack.dao;

import com.roomstack.entity.PGEntity;
import com.roomstack.entity.PGImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PGImageRepository extends JpaRepository<PGImage ,Long> {

    List<PGImage> findByPgEntity(PGEntity pgEntity);
    List<PGImage> findByPgEntityId(Long id);

}
