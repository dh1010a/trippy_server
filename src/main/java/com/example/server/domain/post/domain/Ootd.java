package com.example.server.domain.post.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="post_ootd")
public class Ootd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ootd_id")
    private Long id;

    private String area;

    @Column(name="weather_status")
    private String weatherStatus;

    @Column(name="weather_temp")
    private String weatherTemp;

    @Column(name="detail_location")
    private String detailLocation;

    private LocalDate date;

    @OneToOne(mappedBy = "ootd", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Post post;
}
