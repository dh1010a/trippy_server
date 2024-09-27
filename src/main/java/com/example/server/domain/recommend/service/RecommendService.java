package com.example.server.domain.recommend.service;

import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.InterestedType;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.service.PostService;
import com.example.server.domain.recommend.dto.RecommendRequestDto;
import com.example.server.domain.recommend.dto.RecommendResponseDto;
import com.example.server.domain.recommend.dto.RecommendResponseDto.PlaceImageDto;
import com.example.server.domain.recommend.dto.RecommendResponseDto.PlaceImageResponseDto;
import com.example.server.domain.search.service.SearchRedisService;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final PostRepository postRepository;
    private final PostService postService;
    private final SearchRedisService searchRedisService;
    private final RestTemplate restTemplate;
    private final MemberFollowRepository memberFollowRepository;

    private static final String ANONYMOUS = "anonymousUser";

    @Value("${spring.security.oauth2.client.registration.kakao.clientId}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.clientSecret}")
    private String kakaoClientSecret;

    private static final String kakaoSearchUrl = "https://dapi.kakao.com/v2/search/image";

    @Value("${openApi_img.img_serviceKey}")
    private String serviceKey;

    @Value("${openApi_img.img_endPoint}")
    private String endPoint;

    @Value("${openApi_img.img_dataType}")
    private String dataType;

    public List<String> getRecommendSearch(String memberId){
        Set<String> mergedSet = new HashSet<>();
        Member member = postService.getMemberById(memberId);
        RecommendRequestDto.GetRecommendRequest getRecommendRequestPOST = createRecommendRequest(member, PostType.POST);
        RecommendRequestDto.GetRecommendRequest getRecommendRequestOOTD = createRecommendRequest(member, PostType.OOTD);
        List<String> recommendKeywordsPOST = getRecommendKeywordsFromFlask(getRecommendRequestPOST);
        List<String> recommendKeywordsOOTD = getRecommendKeywordsFromFlask(getRecommendRequestOOTD);
        mergedSet.addAll(recommendKeywordsOOTD);
        mergedSet.addAll(recommendKeywordsPOST);
        return new ArrayList<>(mergedSet);

    }

    public List<RecommendResponseDto.RecommendPlaceResponseDto> getRecommendSpot(Long postId) {
        Post post = postService.getPostById(postId);
        String area;
        if(post.getPostType().equals(PostType.OOTD)) {
            String address = post.getOotd().getDetailLocation();
            area = extractCityOrCountyName(address);
            System.out.println(area);

        } else  area = post.getTicket().getDestination();

        List<String> recommendSpot = getRecommendSpotFromFlask(area);
        List<RecommendResponseDto.RecommendPlaceResponseDto> result = new ArrayList<>();
        for (String spot : recommendSpot) {
            try {
                String encodedSpot = URLEncoder.encode(spot, StandardCharsets.UTF_8);

                DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(endPoint);
                factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

                WebClient webClient = WebClient.builder()
                        .uriBuilderFactory(factory)
                        .baseUrl(endPoint)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();
                String response = webClient.get()
                        .uri(uriBuilder -> {
                            return uriBuilder.path("/galleryDetailList1")
                                    .queryParam("serviceKey", serviceKey)
                                    .queryParam("dataType", dataType)
                                    .queryParam("MobileApp", "AppTest")
                                    .queryParam("MobileOS", "ETC")
                                    .queryParam("_type", "Json")
                                    .queryParam("numOfRows", "10")
                                    .queryParam("title", spot)

//                            .queryParam("cond[country_nm::EQ]", "가나")
                                    .build(true);
                        })
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                PlaceImageResponseDto dto = null;
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    dto = mapper.readValue(response, PlaceImageResponseDto.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (dto != null) {
                    List<PlaceImageDto> imgUrl = dto.getItem();
                    if (imgUrl.size() > 0) {
                        RecommendResponseDto.RecommendPlaceResponseDto recommendPlaceResponseDto = RecommendResponseDto.RecommendPlaceResponseDto.builder()
                                .title(spot)
                                .hubTatsNm(spot)
                                .imgUrl(dto.getItem())
                                .content(spot + " 관광 추천")
                                .imgCnt(dto.getNumOfRows())
                                .build();
                        result.add(recommendPlaceResponseDto);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    public static String extractCityOrCountyName(String address) {

        Pattern pattern = Pattern.compile("([가-힣]+?)(?=광역시|특별시|특별자치시|시|군)");
        Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            return matcher.group(1);
        }
        else throw new ErrorHandler(ErrorStatus.INVALID_CITY_NAME);
    }

    public List<String> getRecommendSpotFromFlask(String area) {
        String FLASK_URL = "http://flask-app:5000/api/find_area";
        try {
            String encodedArea = URLEncoder.encode(area, StandardCharsets.UTF_8.toString());
            String url = FLASK_URL + "?input=" + encodedArea;

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<String> result = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                String hubTatsNm = node.get("hubTatsNm").asText();
                result.add(hubTatsNm);
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public PostResponseDto.GetMultiplePostResponseDto getRecommendPosts(String interest, PostType postType, String memberId){
        Member member = postService.getMemberById(memberId);
        List<Integer> postIds = getRecommendInterest(interest, postType, memberId);
        List<Post> posts = new ArrayList<>();
        // RecommendRequest 객체 생성
        for (int x: postIds){
            Post post = postService.getPostById((long)x);
            if (!isValidAccessToPost(post, member, postType)) {
                continue;
            }
            posts.add(post);
        }
        return PostDtoConverter.convertToMultiplePostResponseDto(posts, member);
    }

    public PostResponseDto.GetMultipleOotdResponseDto getRecommendOotds(String interest, PostType postType, String memberId){
        Member member = postService.getMemberById(memberId);
        List<Integer> ootdIds = getRecommendInterest(interest, postType, memberId);
        List<Post> ootds = new ArrayList<>();
        // RecommendRequest 객체 생성
        for (int x: ootdIds){
            Post post = postService.getPostById((long)x);
            if (!isValidAccessToPost(post, member, postType)) {
                continue;
            }
            ootds.add(post);
        }
        return PostDtoConverter.convertToMultipleOotdResponseDto(ootds, member);
    }

    private boolean isValidAccessToPost(Post post, Member member, PostType postType) {
        if (postType == PostType.OOTD) {
            if (post.getMember().getOotdScope() == Scope.PRIVATE) {
                return false;
            }
            if (post.getMember().getOotdScope() == Scope.PROTECTED) {
                if (member == null) {
                    return false;
                }
                return isFollower(member, post.getMember());
            }

        } else if (postType == PostType.POST) {
            if (post.getMember().getTicketScope() == Scope.PRIVATE) {
                return false;
            }
            if (post.getMember().getTicketScope() == Scope.PROTECTED) {
                if (member == null) {
                    return false;
                }
                return isFollower(member, post.getMember());
            }
        }
        return true;
    }

    private boolean isFollower(Member member, Member targetMember) {
        return memberFollowRepository.existsByMemberAndFollowingMemberIdx(member, targetMember.getIdx());
    }

    // 추천 게시물 요청
    private List<Integer> getRecommendInterest(String interest, PostType postType, String memberId) {
        String FLASK_URL = "http://flask-app:5000/api/interest_posts";

        InterestedType interestedType = InterestedType.fromKoreanName(interest);
        if (interestedType == null) {
            throw new ErrorHandler(ErrorStatus.MEMBER_INTEREST_TYPE_NOT_VALID);
        }
        //
        int memberIdx = -1;
        if(!memberId.equals("anonymousUser")){
            Member member = postService.getMemberById(memberId);
            memberIdx = member.getIdx().intValue();
        }
        String url = UriComponentsBuilder.fromHttpUrl(FLASK_URL)
                .queryParam("interest", interestedType)
                .queryParam("post_type", postType)
                .queryParam("member_idx", memberIdx)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            String responseBody = responseEntity.getBody();
            System.out.println("Flask 서버 응답: " + responseBody);

            return parsePostIds(responseBody);

        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 오류 발생: " + e.getStatusCode());
            System.err.println("오류 메시지: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("예기치 못한 오류 발생: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    private List<Integer> parsePostIds(String responseBody) {
        List<Integer> postIds = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode postIdsNode = root.path("post_ids");

            if (postIdsNode.isArray()) {
                for (JsonNode node : postIdsNode) {
                    postIds.add(node.asInt());
                }
            }
        } catch (Exception e) {
            System.err.println("JSON 파싱 오류 발생: " + e.getMessage());
        }
        return postIds;
    }

    private RecommendRequestDto.GetRecommendRequest createRecommendRequest(Member member, PostType postType) {
        List<Long> followingList = postService.getFollowingList(member);

        // 회원이 좋아요 한 게시물 리스트
        List<RecommendRequestDto.PostContentDto> likePosts = getLikePosts(member, postType, followingList);

        // 회원의 최근 검색어
        String key1 = "SearchLog" + postType + member.getMemberId();
        List<String> currentSearchList = searchRedisService.getRecentSearch(key1);

        // 인기 검색어
        List<String> popularSearchList = searchRedisService.getPopularListByType(postType);

        // RecommendRequestDto 객체 생성
        return RecommendRequestDto.GetRecommendRequest.builder()
                .currentSearchList(currentSearchList)
                .likePostContentDtoList(likePosts)
                .popularSearchList(popularSearchList)
                .build();
    }

    // 추천 키워드
    private List<String> getRecommendKeywordsFromFlask(RecommendRequestDto.GetRecommendRequest getRecommendRequest) {
        String url = UriComponentsBuilder.fromHttpUrl("http://flask-app:5000/api/recommend/post")
                .toUriString();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // 요청 본문과 헤더를 포함하는 HttpEntity 생성
        HttpEntity<RecommendRequestDto.GetRecommendRequest> requestEntity = new HttpEntity<>(getRecommendRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Flask 서버로 POST 요청을 보내고, String으로 응답을 받습니다.
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 응답 로그 출력
            String responseBody = responseEntity.getBody();
            System.out.println("Flask 서버 응답: " + responseBody);

            // 키워드 문자열을 배열로 변환
            return parseKeywords(responseBody);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 오류 발생: " + e.getStatusCode());
            System.err.println("오류 메시지: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("예기치 못한 오류 발생: " + e.getMessage());
        }

        return new ArrayList<>();
    }



    public List<RecommendRequestDto.PostContentDto> getLikePosts(Member member, PostType postType, List<Long> followingList){
        Pageable pageable = postService.getPageable(0, 10, OrderType.LATEST);
        List<Long> followingMemberIds = postService.getFollowingList(member);
        List<Post> posts = postRepository.findLikedPostsByMemberWithPostTypeAndScope(member.getMemberId(),postType,  followingMemberIds, pageable).getContent();
        List<RecommendRequestDto.PostContentDto> recommendRequestDtoList = new ArrayList<>();
        for(Post post : posts){
            RecommendRequestDto.PostContentDto postContentDto = RecommendRequestDto.PostContentDto.builder()
                    .title(post.getTitle())
                    .body(post.getBody())
                    .build();
            recommendRequestDtoList.add(postContentDto);
        }

        return recommendRequestDtoList;
    }

    public List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(keywords.split(","));
    }

}
