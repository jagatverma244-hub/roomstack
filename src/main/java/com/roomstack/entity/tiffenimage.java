package com.roomstack.entity;

import jakarta.persistence.*;
@Entity
public class tiffenimage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "tiffen_id")
    private Tiffen tiffen;

    public void setId(Long id) {
        this.id = id;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setTiffen(Tiffen tiffen) {
        this.tiffen = tiffen;
    }

    public Long getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public Tiffen getTiffen() {
        return tiffen;
    }
}
