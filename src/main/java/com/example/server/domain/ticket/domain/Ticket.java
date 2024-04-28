package com.example.server.domain.ticket.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ticket_id")
    private Long id;

    private String departure;

    private String destination;

//    @Column(name="title_img")
//    private String titleImg;

    @Column(name="member_num")
    private String memberNum;

    @Column(name="duration")
    private String duration;

    @OneToMany(mappedBy = "ticket")
    @JsonIgnore
    private List<MemberTicket> memberTicket;


}
