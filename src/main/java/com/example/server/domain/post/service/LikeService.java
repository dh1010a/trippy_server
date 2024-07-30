package com.example.server.domain.post.service;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.notify.dto.NotifyDtoConverter;
import com.example.server.domain.notify.model.NotificationType;
import com.example.server.domain.post.domain.Like;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.dto.LikeResponseDto;
import com.example.server.domain.post.repository.LikeRepository;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.server.domain.post.dto.LikeDtoConverter.convertToLikeBasicDto;
import static com.example.server.domain.post.dto.LikeDtoConverter.convertToLikeBasicListDto;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    // POST /api/like/{postId}
    public LikeResponseDto.LikeBasicResponseDto likeToPost(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        Member member = memberRepository.getMemberById(memberId);
        if(isLiked(postId, memberId)) throw new ErrorHandler(ErrorStatus.ALREADY_LIKED);
        else {
            Like like = Like.builder()
                    .post(post)
                    .member(member)
                    .build();
            likeRepository.save(like);
            Integer likeCount = getLikeCount(postId);
            publishLikeEvent(member, post.getMember());
            return convertToLikeBasicDto(like,likeCount);
        }

    }

    // GET /api/like/{postId}
    public LikeResponseDto.LikeListBasicResponseDto PostLikeList(Long postId) {
        List<Like> likes = likeRepository.findAllByPostId(postId);
        return convertToLikeBasicListDto(likes,getLikeCount(postId),postId);
    }


    // GET /api/like/isLiked
    public Boolean isLiked(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND)); Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        if(likeRepository.findByPostIdAndMemberIdx(post.getId(),member.getIdx()).isPresent()){
            return true;
        }
        else return false;
    }

    // DELETE /api/like/{postId}
    public Boolean deletePostLike(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND)); Member member = memberRepository.findByMemberId(memberId).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        if(isLiked(postId, memberId)) {
            Like like =likeRepository.findByPostIdAndMemberIdx(postId,member.getIdx()).get();
            likeRepository.delete(like);
            return true;
        }
        else throw new ErrorHandler(ErrorStatus.POST_NOT_LIKED);
    }

    public Integer getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    //== 알림을 보내는 기능 ==//
    public void publishLikeEvent(Member member, Member receiver) {
        eventPublisher.publishEvent(NotifyDtoConverter.convertToNotifyPublishRequestDto(member, receiver, NotificationType.LIKE));
    }

}
