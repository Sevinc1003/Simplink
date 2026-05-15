package com.simplink.main.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(
            mappedBy = "url",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<IpLog> ipLogs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Url(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
