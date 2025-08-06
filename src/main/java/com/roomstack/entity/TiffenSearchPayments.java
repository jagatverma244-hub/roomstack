package com.roomstack.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class TiffenSearchPayments
{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private Integer amount;
    private String status;

    private Timestamp paymentTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tiffen_id")
    private Tiffen tiffen;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tiffen_owner_id")
    private User tiffenOwner;

    public void setId(Long id) {
        this.id = id;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentTime(Timestamp paymentTime) {
        this.paymentTime = paymentTime;
    }

    public void setTiffen(Tiffen tiffen) {
        this.tiffen = tiffen;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTiffenOwner(User tiffenOwner) {
        this.tiffenOwner = tiffenOwner;
    }

    public Long getId() {
        return id;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getPaymentTime() {
        return paymentTime;
    }

    public Tiffen getTiffen() {
        return tiffen;
    }

    public User getUser() {
        return user;
    }

    public User getTiffenOwner() {
        return tiffenOwner;
    }
}
