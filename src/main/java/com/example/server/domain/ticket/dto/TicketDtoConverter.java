package com.example.server.domain.ticket.dto;

import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.ticket.domain.Ticket;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;

public class TicketDtoConverter {

    public static TicketResponseDto.TicketBasicResponseDto convertToTicketResponseDto(Ticket ticket){

        ImageResponseDto.ImageBasicResponseDto ticketImage = convertToImageBasicDto(ticket.getImage());
        TicketResponseDto.TicketBasicResponseDto result = TicketResponseDto.TicketBasicResponseDto.builder()
                .id(ticket.getId())
                .ticketColor(ticket.getTicketColor())
                .departure(ticket.getDeparture())
                .destination(ticket.getDestination())
                .transport(ticket.getTransport())
                .memberNum(ticket.getMemberNum())
                .startDate(ticket.getStartDate())
                .endDate(ticket.getEndDate())
                .image(ticketImage)
                .build();
        return result;
    }
}
