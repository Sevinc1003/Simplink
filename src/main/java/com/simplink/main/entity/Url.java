package com.simplink.main.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    public Url(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
