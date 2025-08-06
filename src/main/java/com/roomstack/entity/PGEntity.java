package com.roomstack.entity;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PGEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String PGName;
    private String Address;
    private String Area;
    private String city;
    private String distict;
    private String phonenumber;
    private String GenderType;
    private int Monthlyrent;
    private int SecurityDeposit;
    private int TOtalBedsAvailable;
    private String RoomType;
    private Boolean available;
    @ElementCollection
    private List<String> amenities;

    private String PGRule;
    private String uploadDate;

    @Transient
    private List<MultipartFile> imageFiles; // only for temporary upload handling

    @OneToMany(mappedBy = "pgEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PGImage> pgimages = new ArrayList<>();
    @OneToOne(mappedBy = "pgEntity", cascade = CascadeType.ALL)
    private PGPaments pgpayments;

    @OneToMany(mappedBy = "pgEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<searchPGpaments> searchpgPayments = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // This is the room owner



    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setMonthlyrent(Integer monthlyrent) {
        Monthlyrent = monthlyrent;
    }

    public void setSecurityDeposit(Integer securityDeposit) {
        SecurityDeposit = securityDeposit;
    }

    public void setTOtalBedsAvailable(Integer TOtalBedsAvailable) {
        this.TOtalBedsAvailable = TOtalBedsAvailable;
    }

    public void setPgpayments(PGPaments pgpayments) {
        this.pgpayments = pgpayments;
    }

    public void setSearchpgPayments(List<searchPGpaments> searchpgPayments) {
        this.searchpgPayments = searchpgPayments;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PGPaments getPgpayments() {
        return pgpayments;
    }

    public List<searchPGpaments> getSearchpgPayments() {
        return searchpgPayments;
    }

    public User getUser() {
        return user;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setPGName(String PGName) {
        this.PGName = PGName;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setArea(String area) {
        Area = area;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistict(String distict) {
        this.distict = distict;
    }


    public void setGenderType(String genderType) {
        GenderType = genderType;
    }

    public void setMonthlyrent(int monthlyrent) {
        Monthlyrent = monthlyrent;
    }

    public void setSecurityDeposit(int securityDeposit) {
        SecurityDeposit = securityDeposit;
    }

    public void setTOtalBedsAvailable(int TOtalBedsAvailable) {
        this.TOtalBedsAvailable = TOtalBedsAvailable;
    }

    public void setRoomType(String roomType) {
        RoomType = roomType;
    }


    public void setPGRule(String PGRule) {
        this.PGRule = PGRule;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public void setPgimages(List<PGImage> pgimages) {
        this.pgimages = pgimages;
    }

    public Long getId() {
        return id;
    }

    public String getPGName() {
        return PGName;
    }

    public String getAddress() {
        return Address;
    }

    public String getArea() {
        return Area;
    }

    public String getCity() {
        return city;
    }

    public String getDistict() {
        return distict;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public String getGenderType() {
        return GenderType;
    }

    public int getMonthlyrent() {
        return Monthlyrent;
    }

    public int getSecurityDeposit() {
        return SecurityDeposit;
    }

    public int getTOtalBedsAvailable() {
        return TOtalBedsAvailable;
    }

    public String getRoomType() {
        return RoomType;
    }


    public String getPGRule() {
        return PGRule;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }

    public List<PGImage> getPgimages() {
        return pgimages;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getPhonenumber() {
        return phonenumber;
    }
}
