package com.example.server.domain.ticket.domain;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.domain.ticket.model.TicketColor;
import com.example.server.domain.ticket.model.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    private String departureCode;

    private String destinationCode;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name="member_num")
    private Integer memberNum;

//    @Column(name="duration")
//    private String duration;

    @Column(name="ticket_color")
    @Enumerated(EnumType.STRING)
    private TicketColor ticketColor;

    @Column(name="transport")
    @Enumerated(EnumType.STRING)
    private Transport transport;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Post post;

//
//    @OneToMany(mappedBy = "ticket")
//    @JsonIgnore
//    private List<MemberTicket> memberTicket;


    public void updatePost(Post post){this.post=post;}
    public void updateImage(Image image){this.image=image;}

    public void updateTicket(TicketRequestDto.UpdateTicketRequestDto requestDto){
        this.departure = requestDto.getDeparture();
        this.destination = requestDto.getDestination();
        this.departureCode = requestDto.getDepartureCode();
        this.destinationCode = requestDto.getDestinationCode();
        this.memberNum = requestDto.getMemberNum();
        this.ticketColor = requestDto.getTicketColor();
        this.transport = requestDto.getTransport();
        this.startDate = requestDto.getStartDate();
        this.endDate = requestDto.getEndDate();
    }
}
