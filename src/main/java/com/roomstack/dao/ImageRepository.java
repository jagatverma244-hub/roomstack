package com.roomstack.dao;

import com.roomstack.entity.Image;
import com.roomstack.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image ,Long> {



        List<Image> findByRoom(Room room);
    List<Image> findByRoomId(Long id);

    }


