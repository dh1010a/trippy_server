package com.example.server.domain.ticket.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.domain.ticket.dto.TicketResponseDto;
import com.example.server.domain.ticket.repository.TicketRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.example.server.domain.ticket.dto.TicketDtoConverter.convertToTicketResponseDto;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;

    private final ImageRepository imageRepository;

    public TicketResponseDto.TicketBasicResponseDto updateTicket(TicketRequestDto.UpdateTicketRequestDto requestDto){
        Ticket ticket = ticketRepository.findById(requestDto.getId()).orElseThrow(() -> new ErrorHandler(ErrorStatus.TICKET_NOT_FOUND));
        // 이미지가 변했으면
        if(ticket.getImage().getImgUrl() != requestDto.getImage().getImgUrl()){
            Image newImage = Image.builder()
                    .imgUrl(requestDto.getImage().getImgUrl())
                    .accessUri(requestDto.getImage().getAccessUri())
                    .authenticateId(requestDto.getImage().getAuthenticateId())
                    .imageType(ImageType.TICKET)
                    .post(ticket.getPost())
                    .build();
            imageRepository.delete(ticket.getImage());
            imageRepository.save(newImage);
            ticket.updateImage(newImage);
        }
        ticket.updateTicket(requestDto);
        return convertToTicketResponseDto(ticket);
    }



}
