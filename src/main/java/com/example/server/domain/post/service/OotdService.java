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
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.OotdRepository;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.server.domain.post.dto.PostDtoConverter.convertToOotdBasicResponseDto;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OotdService {
    private final OotdRepository ootdRepository;
    private final PostService postService;
    private final PostRepository postRepository;

    private final RestTemplate restTemplate;


    // POST /api/ootd
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

    // GET /api/ootd/{id}
    public PostResponseDto.GetOotdPostResponseDto getPost(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if(post.getPostType() == PostType.POST) {
            throw new ErrorHandler(ErrorStatus.OOTD_TYPE_ERROR);
        }
        return PostDtoConverter.convertToOotdResponseDto(post);
    }

    // GET api/ootd/all
    public List<PostResponseDto.GetOotdPostResponseDto> getAllPost(Integer page, Integer size){
        // 둘다 0일때 => 변수 입력 안받음
        if(page==0 && size==0){
            List<Post> postList = postRepository.findAllByPostType(PostType.OOTD);
            return PostDtoConverter.convertToOOTDListResponseDto(postList);
        }
        else {
            PageRequest pageable = PageRequest.of(page, size);
            List<Post> postList = postRepository.findAllByPostType(PostType.OOTD,pageable).getContent();
            return PostDtoConverter.convertToOOTDListResponseDto(postList);
        }
    }

    // GET api/ootd/
    public List<PostResponseDto.GetOotdPostResponseDto> getAllMemberPost(String memberId,Integer page, Integer size){
        Member member = postService.getMember(memberId);
        // 둘다 0일때 => 변수 입력 안받음
        if(page==0 && size==0){
            List<Post> postList = postRepository.findAllByMemberAndPostType(member,PostType.OOTD);
            return PostDtoConverter.convertToOOTDListResponseDto(postList);
        }
        else {
            Pageable pageable = PageRequest.of(page, size);
            List<Post> postList = postRepository.findAllByMemberAndPostType(member,PostType.OOTD, pageable).getContent();
            return PostDtoConverter.convertToOOTDListResponseDto(postList);
        }

    }

    // PATCH api/ootd
    public OotdReqResDto.OotdBasicResponseDto updateOotd(String memberId, OotdReqResDto.UpdateOOTDRequestDto updateOOTDRequestDto){
        Optional<Ootd> ootd = ootdRepository.findById(updateOOTDRequestDto.getId());
        if(ootd.isPresent()){
            if(Objects.equals(ootd.get().getPost().getMember().getMemberId(), memberId)){
                ootd.get().updateOotd(updateOOTDRequestDto);
                return convertToOotdBasicResponseDto(ootd.get());
            }
            else {
                throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
            }
        }
        else {
            throw new ErrorHandler(ErrorStatus.OOTD_NOT_FOUND);
        }
    }

    public OotdReqResDto.WeatherResponseDto callFlaskGetWeather(OotdReqResDto.WeatherRequestDto weatherRequestDto) {
        // URL 빌더 생성
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://flask-app:5000/api/weather")
                .queryParam("latitude", weatherRequestDto.getLatitude())
                .queryParam("longitude", weatherRequestDto.getLongitude())
                .queryParam("date", weatherRequestDto.getDate());

        // 완성된 URL 문자열
        String url = builder.toUriString();

        // GET 요청 보내기
        String response = restTemplate.getForObject(url, String.class).replace("\"", "");
        if(response == "500") {
            throw new ErrorHandler(ErrorStatus.ERROR_WHILE_GET_WEATHER);
        }
        else if(response == "4001"){
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION_NATION);
        }
        else {
            String[] responseArray = response.split(",");

            // 순서 : avg, max, min, status, date,area
            OotdReqResDto.WeatherResponseDto weatherResponseDto = OotdReqResDto.WeatherResponseDto.builder()
                    .avgTemp(responseArray[0])
                    .maxTemp(responseArray[1])
                    .minTemp(responseArray[2])
                    .status(responseArray[3])
                    .area(responseArray[4])
                    .build();

            return weatherResponseDto;
        }

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
