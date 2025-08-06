package com.roomstack.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
@Entity
public class Roompayments {

    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private Integer amount;
    private String status;
    private Timestamp paymentTime;

    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }



    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentTime(Timestamp paymentTime) {
        this.paymentTime = paymentTime;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    public void setAmount(Integer amount) {
        this.amount = amount;
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

    public Room getRoom() {
        return room;
    }

    // Getters, setters
}


