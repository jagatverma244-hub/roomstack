package com.roomstack.entity;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Entity


public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String email;
    //private String password;
    private String mobile;
    //private String role;
    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;
    @Transient
    private MultipartFile file;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    // OR
//    private LocalDateTime createdAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "login_id")
    private Login login;

    public void setLogin(Login login) {
        this.login = login;
    }

    public Login getLogin() {
        return login;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PGEntity> pgEntities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tiffen> tiffen = new ArrayList<>();


    public List<PGEntity> getPgEntities() {
        return pgEntities;
    }

    public void setPgEntities(List<PGEntity> pgEntities) {
        this.pgEntities = pgEntities;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }


    public MultipartFile getFile() {
        return file;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }


    public String getMobile() {
        return mobile;
    }


    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setTiffen(List<Tiffen> tiffen) {
        this.tiffen = tiffen;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public List<Tiffen> getTiffen() {
        return tiffen;
    }
}
