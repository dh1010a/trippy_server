package com.example.server.domain.ticket.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.ticket.model.TicketColor;
import com.example.server.domain.ticket.model.Transport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class TicketRequestDto {
    @Builder
    @Data
    public static class UploadTicketRequestDto{
        private String departure;
        private String destination;
        private String departureCode;
        private String destinationCode;
        private ImageDto image;
        private Integer memberNum;
//        private String duration;
        private TicketColor ticketColor;
        private Transport transport;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Builder
    @Data
    public static class UpdateTicketRequestDto{
        private Long id;
        private String departure;
        private String destination;
        private String departureCode;
        private String destinationCode;
        private ImageDto image;
        private Integer memberNum;
        //        private String duration;
        private TicketColor ticketColor;
        private Transport transport;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
