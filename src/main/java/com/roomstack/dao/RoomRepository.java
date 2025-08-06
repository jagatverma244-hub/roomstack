package com.roomstack.dao;

import com.roomstack.entity.Room;
import com.roomstack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByUser(User user);
    List<Room> findByUserId(Long userId);
//    Room findByUser_Id(Long id);
    int countByUserId(Long userId);
//    Page<Room> findByLocationCityAndLocationAreaAndAndPrice(String locationcity, String LocationArea, Long price, Pageable pageable);
@Query("SELECT r FROM Room r where "+"LOWER(r.location.city) like LOWER(concat('%',:keyword,'%'))"+
"OR LOWER(r.location.area) LIKE LOWER(CONCAT('%',:keyword,'%'))"+
        "OR CAST(r.price AS string )LIKE  %:keyword%")
    Page<Room> searchRooms(@Param("keyword") String keyword,Pageable pageable);


}
