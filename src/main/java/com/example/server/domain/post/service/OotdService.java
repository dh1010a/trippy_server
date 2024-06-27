package com.example.server.domain.post.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Ootd;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.OotdReqResDto;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.repository.OotdRepository;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OotdService {
    private final OotdRepository ootdRepository;
    private final PostService postService;
    private final PostRepository postRepository;

    @Transactional
    public PostResponseDto.GetOotdPostResponseDto uploadOotdPost(PostRequestDto.UploadOOTDPostRequestDto requestDto) {
        PostRequestDto.CommonPostRequestDto postRequestDto = requestDto.getPostRequest();
        Member member = postService.getMember(postRequestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND);
        Post post = postService.savePost(postRequestDto);

        OotdReqResDto.UploadOOTDRequestDto ootdRequestDto = requestDto.getOotdRequest();

        if (postRequestDto.getImages() != null) {
            List<Image> images = postService.saveImages(postRequestDto,post);
            post.updateImages(images);
        }
        if(postRequestDto.getTags() != null) {
            List<Tag> tags = postService.saveTags(postRequestDto, post);
            post.updateTags(tags);
        }

        Ootd ootd = saveOotd(ootdRequestDto);
        savePostAndOOTDAndAll(post,ootd);
        return PostDtoConverter.convertToOotdResponseDto(post);
    }

    @Transactional
    public void savePostAndOOTDAndAll(Post post, Ootd ootd) {
        ootdRepository.save(ootd);
        post.updateOotd(ootd);
        postRepository.save(post);
    }

    public Ootd saveOotd(OotdReqResDto.UploadOOTDRequestDto requestDto){
        Ootd ootd = Ootd.builder()
                .area(requestDto.getArea())
                .date(requestDto.getDate())
                .weatherTemp(requestDto.getWeatherTemp())
                .weatherStatus(requestDto.getWeatherStatus())
                .detailLocation(requestDto.getDetailLocation())
                .build();
        return ootd;
    }

}
