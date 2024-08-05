package com.example.server.domain.ticket.dto;

import com.example.server.domain.country.service.CountryService;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.ticket.domain.Ticket;

import static com.example.server.domain.image.dto.ImageDtoConverter.convertToImageBasicDto;

public class TicketDtoConverter {

    private final CountryService countryService;

    public TicketDtoConverter(CountryService countryService) {
        this.countryService = countryService;
    }

    public static TicketResponseDto.TicketBasicResponseDto convertToTicketResponseDto(Ticket ticket){

        ImageResponseDto.ImageBasicResponseDto ticketImage = convertToImageBasicDto(ticket.getImage());
        TicketResponseDto.TicketBasicResponseDto result = TicketResponseDto.TicketBasicResponseDto.builder()
                .id(ticket.getId())
                .ticketColor(ticket.getTicketColor())
                .departure(ticket.getDeparture())
                .destination(ticket.getDestination())
                .departureCode(ticket.getDepartureCode() != null ? ticket.getDepartureCode() : null)
                .destinationCode(ticket.getDestinationCode() != null ? ticket.getDestinationCode() : null)
                .transport(ticket.getTransport())
                .memberNum(ticket.getMemberNum())
                .startDate(ticket.getStartDate())
                .endDate(ticket.getEndDate())
                .image(ticketImage)
                .build();
        return result;
    }
}
