package com.example.server.domain.recommend.service;

import aj.org.objectweb.asm.TypeReference;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.service.PostService;
import com.example.server.domain.recommend.dto.RecommendRequestDto;
import com.example.server.domain.search.service.SearchRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final PostRepository postRepository;
    private final PostService postService;
    private final SearchRedisService searchRedisService;
    private final RestTemplate restTemplate;

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


    public List<PostResponseDto.GetPostResponseDto> getRecommendPosts(String memberId, PostType postType){
        Member member = postService.getMemberById(memberId);

        // RecommendRequest 객체 생성
        RecommendRequestDto.GetRecommendRequest getRecommendRequest = createRecommendRequest(member, postType);
        return null;
    }

    public List<PostResponseDto.GetOotdPostResponseDto> getRecommendOotds(String memberId, PostType postType){
        Member member = postService.getMemberById(memberId);

        // RecommendRequest 객체 생성
        RecommendRequestDto.GetRecommendRequest getRecommendRequest = createRecommendRequest(member, postType);
        return null;
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
