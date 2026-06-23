package com.pbl.recycleai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    @Column(name = "path")
    private String path;

    @Column(name = "uploadDate")
    private String uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "binId", nullable = false)
    @JsonIgnore
    private Bin bin;

    public Image(Integer imageId, String path, Bin bin) {
        this.imageId = imageId;
        this.path = path;
        this.bin = bin;
    }
}
