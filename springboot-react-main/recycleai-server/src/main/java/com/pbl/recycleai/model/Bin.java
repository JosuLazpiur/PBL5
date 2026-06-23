package com.pbl.recycleai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "bin")
public class Bin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer binId;

    @Column(name = "operative")
    private Boolean operative;

    @Column(name = "ubication")
    private String ubication;

    @ManyToOne
    @JoinColumn(name = "domainId", nullable = false)
    private Domain domain;

    public Bin(Integer binId, String ubication, Domain domain) {
        this.binId = binId;
        this.ubication = ubication;
        this.domain = domain;
    }

    public Boolean isOperative(){
        return operative;
    }

}
