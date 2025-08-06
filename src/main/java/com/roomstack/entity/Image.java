package com.roomstack.entity;

import jakarta.persistence.*;

@Entity

public class Image {
@Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;


    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public void setRoom(Room room) {
        this.room = room;
    }

    public Long getId() {
        return id;
    }


    public Room getRoom() {
        return room;
    }
}
