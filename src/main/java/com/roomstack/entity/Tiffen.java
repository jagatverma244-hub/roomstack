package com.roomstack.entity;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

//tiffen
@Entity
public class Tiffen {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String tiffencentername;
    private String phonenumber;
    private String Area;
    private String deeplocation;
    private String city;
    private String district;
    private String tiffentype;
    private Long pricePerMonth;
    private String description;
    private String mealType;
    private Boolean Available;
    private String dayavailable;
    private String includes;
    private boolean deliveryAvailable;
    private String areaCovered;

    @Transient
    private List<MultipartFile> imageFiles; // only for temporary upload handling

    @OneToMany(mappedBy = "tiffen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<tiffenimage> tiffenimages = new ArrayList<>();
    @OneToOne(mappedBy = "tiffen", cascade = CascadeType.ALL)
    private TiffenPayments tiffenpayments;

    @OneToMany(mappedBy = "tiffen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TiffenSearchPayments> searchtiffenPayments = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // This is the room owner

    public void setTiffenimages(List<tiffenimage> tiffenimages) {
        this.tiffenimages = tiffenimages;
    }

    public Boolean getAvailable() {
        return Available;
    }

    public void setAvailable(Boolean available) {
        Available = available;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTiffencentername(String tiffencentername) {
        this.tiffencentername = tiffencentername;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setArea(String area) {
        Area = area;
    }

    public void setDeeplocation(String deeplocation) {
        this.deeplocation = deeplocation;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setTiffentype(String tiffentype) {
        this.tiffentype = tiffentype;
    }

    public void setPricePerMonth(Long pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public void setDayavailable(String dayavailable) {
        this.dayavailable = dayavailable;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public void setDeliveryAvailable(boolean deliveryAvailable) {
        this.deliveryAvailable = deliveryAvailable;
    }

    public void setAreaCovered(String areaCovered) {
        this.areaCovered = areaCovered;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }



    public void setTiffenpayments(TiffenPayments tiffenpayments) {
        this.tiffenpayments = tiffenpayments;
    }



    public void setUser(User user) {
        this.user = user;
    }

    public String getTiffencentername() {
        return tiffencentername;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getArea() {
        return Area;
    }

    public String getDeeplocation() {
        return deeplocation;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getTiffentype() {
        return tiffentype;
    }

    public Long getPricePerMonth() {
        return pricePerMonth;
    }

    public String getDescription() {
        return description;
    }

    public String getMealType() {
        return mealType;
    }

    public String getDayavailable() {
        return dayavailable;
    }

    public String getIncludes() {
        return includes;
    }

    public boolean isDeliveryAvailable() {
        return deliveryAvailable;
    }

    public String getAreaCovered() {
        return areaCovered;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }



    public List<tiffenimage> getTiffenimages() {
        return tiffenimages;
    }

    public TiffenPayments getTiffenpayments() {
        return tiffenpayments;
    }

    public List<TiffenSearchPayments> getSearchtiffenPayments() {
        return searchtiffenPayments;
    }

    public void setSearchtiffenPayments(List<TiffenSearchPayments> searchtiffenPayments) {
        this.searchtiffenPayments = searchtiffenPayments;
    }

    public User getUser() {
        return user;
    }
}
