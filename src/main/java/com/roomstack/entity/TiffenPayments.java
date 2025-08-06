package com.roomstack.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class TiffenPayments {
    @Id

    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;

    private Integer amount;
    private String status;
    private Timestamp paymentTime;

    @OneToOne
    @JoinColumn(name = "tiffen_id")
    private Tiffen tiffen;

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

    public String getRazorpaySignature() {
        return razorpaySignature;
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

    public Long getId() {
        return id;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
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
}
