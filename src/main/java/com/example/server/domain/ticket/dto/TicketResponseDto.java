package com.example.server.domain.ticket.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.TagResponseDto;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.ticket.model.TicketColor;
import com.example.server.domain.ticket.model.Transport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

public class TicketResponseDto {

    @Builder
    @Data
    public static class TicketBasicResponseDto {
        private Long id;
        private String departure;
        private String destination;
        private String departureCode;
        private String destinationCode;
        private ImageResponseDto.ImageBasicResponseDto image;
        private Integer memberNum;
        private LocalDate startDate;
        private LocalDate endDate;
        private TicketColor ticketColor;
        private Transport transport;
    }
}
