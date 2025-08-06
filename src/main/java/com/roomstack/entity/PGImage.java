package com.roomstack.entity;

import jakarta.persistence.*;

@Entity

public class PGImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "pg_id")
    private PGEntity pgEntity;


    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public void setId(Long id) {
        this.id = id;
    }





    public Long getId() {
        return id;
    }


    public void setPgEntity(PGEntity pgEntity) {
        this.pgEntity = pgEntity;
    }

    public PGEntity getPgEntity() {
        return pgEntity;
    }
}
