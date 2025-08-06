package com.roomstack.entity;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity

public class Room {
@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
private String descripation;
private Integer price;

private String roomType;
    private Boolean available;

private String uploadDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // This is the room owner


    // Room.java
    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL)
    private Location location;
    @Transient
    private List<MultipartFile> imageFiles; // only for temporary upload handling

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL)
    private Roompayments roompayments;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<searchroompaments> searchRoomPayments = new ArrayList<>();


    public void setSearchRoomPayments(List<searchroompaments> searchRoomPayments) {
        this.searchRoomPayments = searchRoomPayments;
    }

    public List<searchroompaments> getSearchRoomPayments() {
        return searchRoomPayments;
    }

    public void setRoompayments(Roompayments roompayments) {
        this.roompayments = roompayments;
    }

    public Roompayments getRoompayments() {
        return roompayments;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescripation(String descripation) {
        this.descripation = descripation;
    }




    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }


    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescripation() {
        return descripation;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPrice() {
        return price;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Boolean getAvailable() {
        return available;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public User getUser() {
        return user;
    }
}
