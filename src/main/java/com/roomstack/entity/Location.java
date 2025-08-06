package com.roomstack.entity;

import jakarta.persistence.*;


@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long id;
    private String Homeno;
    private String street;
    private String area;
    private String city;
    private String district;

    // Location.java
    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public void setRoom(Room room) {
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHomeno(String homeno) {
        Homeno = homeno;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Long getId() {
        return id;
    }

    public String getHomeno() {
        return Homeno;
    }

    public String getStreet() {
        return street;
    }

    public String getArea() {
        return area;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }
}
