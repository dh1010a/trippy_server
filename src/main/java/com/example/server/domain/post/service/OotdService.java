package com.example.server.domain.post.service;

import com.example.server.domain.country.service.CountryService;
import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Ootd;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.OotdReqResDto;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.OotdRepository;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.server.domain.post.dto.PostDtoConverter.convertToOOTDListResponseDto;
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
    private final MemberFollowRepository memberFollowRepository;
    private final TagRepository tagRepository;
    private final CountryService countryService;

    // POST /api/ootd
    @Transactional
    public PostResponseDto.GetOotdPostResponseDto uploadOotdPost(PostRequestDto.UploadOOTDPostRequestDto requestDto) {
        PostRequestDto.CommonPostRequestDto postRequestDto = requestDto.getPostRequest();
        Member member = postService.getMemberById(postRequestDto.getMemberId());
        Post post = postService.savePost(postRequestDto);

        if (postRequestDto.getImages() != null) {
            List<Image> images = postService.saveImages(postRequestDto, post);
            post.updateImages(images);
        }

        if (postRequestDto.getTags() != null) {
            List<Tag> tags = saveTags(requestDto, post);
            post.updateTags(tags);
        }

        Ootd ootd = saveOotd(requestDto.getOotdRequest());
        savePostAndOotd(post, ootd);

        return PostDtoConverter.convertToOotdResponseDto(post, member);
    }

    // GET /api/ootd/{id}
    public PostResponseDto.GetOotdPostResponseDto getPost(Long postId, String memberId, HttpServletRequest request, HttpServletResponse response) {
        Post post = postService.getPostById(postId);
        if (post.getPostType() != PostType.OOTD) {
            throw new ErrorHandler(ErrorStatus.OOTD_TYPE_ERROR);
        }
        postService.addViewCount(request, postId);

        Member member = postService.getMemberById(memberId);
        return PostDtoConverter.convertToOotdResponseDto(post, member);
    }

    // 전체 게시물
    public List<PostResponseDto.GetOotdPostResponseDto> getAllPost(Integer page, Integer size, String memberId, OrderType orderType) {
        Member member = postService.getMemberById(memberId);
        List<Long> followingList = memberFollowRepository.findFollowingList(member==null ? 0 : member.getIdx());
        Pageable pageable = postService.getPageable(page, size, orderType);
        List<Post> postList = getPostsByOrderType(orderType, pageable, followingList);

        return convertToOOTDListResponseDto(postList,member);

    }

    public List<Post> getPostsByOrderType(OrderType orderType, Pageable pageable, List<Long> followingList) {
        return postRepository.findAllByPostTypeWithScope(PostType.OOTD,followingList, pageable).getContent();
    }



    // 멤버별 게시물
    public List<PostResponseDto.GetOotdPostResponseDto> getAllMemberPost(String memberId, String loginMemberId, Integer page, Integer size, OrderType orderType) {
        Member member = postService.getMemberById(memberId);
        Member loginMember = postService.getMemberById(loginMemberId);
        Pageable pageable = postService.getPageable(page, size, orderType);
        List<Long> followingList = memberFollowRepository.findFollowingList(loginMember==null ? 0 : loginMember.getIdx());

        List<Post> postList = getPostsByOrderTypeAndMember(orderType, pageable, member, followingList);
        return convertToOOTDListResponseDto(postList,loginMember);
    }

    private List<Post> getPostsByOrderTypeAndMember(OrderType orderType, Pageable pageable, Member member, List<Long> followingList) {
        return postRepository.findAllByMemberAndPostType( PostType.OOTD, member, followingList, pageable).getContent();
    }

    // 내 게시물
    public List<PostResponseDto.GetOotdPostResponseDto> getAllMyPost(String loginMemberId, Integer page, Integer size, OrderType orderType) {
        Member loginMember = postService.getMemberById(loginMemberId);
        Pageable pageable = postService.getPageable(page, size, orderType);

        List<Post> postList = getMyPostsByOrderType(orderType, pageable, loginMember);
        return PostDtoConverter.convertToOOTDListResponseDto(postList, loginMember);
    }

    public List<Post> getMyPostsByOrderType(OrderType orderType, Pageable pageable, Member member) {
        return postRepository.findMyPostsWithScore(member,PostType.OOTD, pageable).getContent();
    }


    // PATCH /api/ootd
    public OotdReqResDto.OotdBasicResponseDto updateOotd(String memberId, OotdReqResDto.UpdateOOTDRequestDto requestDto) {
        Ootd ootd = ootdRepository.findById(requestDto.getId())
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.OOTD_NOT_FOUND));

        if (!ootd.getPost().getMember().getMemberId().equals(memberId)) {
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        }

        ootd.updateOotd(requestDto);
        return convertToOotdBasicResponseDto(ootd);
    }

    // Call Flask API to get weather information
    public OotdReqResDto.WeatherResponseDto callFlaskGetWeather(double latitude, double longitude, String date) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://flask-app:5000/api/weather")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("date", date);

        String url = builder.toUriString();
        String response = restTemplate.getForObject(url, String.class).replace("\"", "");

        if (response.equals("500")) {
            throw new ErrorHandler(ErrorStatus.ERROR_WHILE_GET_WEATHER);
        } else if (response.equals("4001")) {
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION_NATION);
        } else {
            String[] responseArray = response.split(",");

            return OotdReqResDto.WeatherResponseDto.builder()
                    .avgTemp(responseArray[0])
                    .maxTemp(responseArray[1])
                    .minTemp(responseArray[2])
                    .status(responseArray[3])
                    .area(responseArray[4])
                    .build();
        }
    }

    // Save OOTD and update post with OOTD
    @Transactional
    public void savePostAndOotd(Post post, Ootd ootd) {
        ootdRepository.save(ootd);
        post.updateOotd(ootd);
        postRepository.save(post);
    }

    // Save OOTD entity
    public Ootd saveOotd(OotdReqResDto.UploadOOTDRequestDto requestDto) {
        return Ootd.builder()
                .area(requestDto.getArea())
                .date(requestDto.getDate())
                .weatherTemp(requestDto.getWeatherTemp())
                .weatherStatus(requestDto.getWeatherStatus())
                .detailLocation(requestDto.getDetailLocation())
                .build();
    }

    // Convert OOTD entity to DTO
    private OotdReqResDto.OotdBasicResponseDto convertToOotdBasicResponseDto(Ootd ootd) {
        return OotdReqResDto.OotdBasicResponseDto.builder()
                .id(ootd.getId())
                .area(ootd.getArea())
                .date(ootd.getDate())
                .weatherTemp(ootd.getWeatherTemp())
                .weatherStatus(ootd.getWeatherStatus())
                .detailLocation(ootd.getDetailLocation())
                .build();
    }

    private List<Tag> saveTags(PostRequestDto.UploadOOTDPostRequestDto requestDto, Post post) {
        List<Tag> collect = new ArrayList<>();
        // 국가와 도시 태그 추가
        String country = countryService.getCountryByLocation(requestDto.getPostRequest().getLocation()).getCountryNm();
        if (country != null) {
            Tag countryTag = Tag.builder()
                    .name(country)
                    .post(post)
                    .build();
            tagRepository.save(countryTag);
            collect.add(countryTag);
        }

        String city = requestDto.getOotdRequest().getArea();
        if (city != null) {
            Tag cityTag = Tag.builder()
                    .name(city)
                    .post(post)
                    .build();
            tagRepository.save(cityTag);
            collect.add(cityTag);
        }

        // 날씨 태그 추가
        if (requestDto.getOotdRequest().getWeatherStatus() != null &&
                !requestDto.getOotdRequest().getWeatherStatus().isEmpty()) {
            String weather = convertWeatherKorean(requestDto.getOotdRequest().getWeatherStatus());
            Tag weatherTag = Tag.builder()
                    .name(weather)
                    .post(post)
                    .build();
            tagRepository.save(weatherTag);
            collect.add(weatherTag);
        }


        for (String tagName : requestDto.getPostRequest().getTags()) {
            Tag tag = Tag.builder()
                    .name(tagName)
                    .post(post)
                    .build();
            tagRepository.save(tag);
            collect.add(tag);
        }
        return collect;
    }

    private String convertWeatherKorean(String weatherEng){
        if (weatherEng == null) return "null";
        return switch (weatherEng.toLowerCase()) {
            case "rain" -> "비";
            case "snow" -> "눈";
            case "mostly_cloudy" -> "구름많음";
            case "cloudy" -> "흐림";
            case "sunny" -> "맑음";
            default -> "null";
        };
    }
}