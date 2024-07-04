package com.example.server.domain.comment.service;

import com.example.server.domain.comment.domain.Comment;
import com.example.server.domain.comment.dto.CommentDtoConverter;
import com.example.server.domain.comment.dto.CommentRequestDto;
import com.example.server.domain.comment.dto.CommentResponseDto;
import com.example.server.domain.comment.model.CommentStatus;
import com.example.server.domain.comment.model.DeleteStatus;
import com.example.server.domain.comment.repository.CommentRepository;
import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Scope;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.catalina.Group;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.server.domain.comment.dto.CommentDtoConverter.converToCommentTreeDTO;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentDtoConverter commentDtoConverter;

    // POST api/comment
    public CommentResponseDto.CommentBasicResponse uploadComment(CommentRequestDto.CommentBasicRequest requestDto) {
        if(requestDto.getParentId()!= null){
            Comment parent = getComment(requestDto.getParentId());
            if(parent.getStatus() == Scope.PRIVATE){
                throw new ErrorHandler(ErrorStatus.PARENT_NOT_FOUND);
            }
        }
        Comment comment = saveComment(requestDto);
        return CommentDtoConverter.convertToCommentBasicResponse(comment);
    }

    // GET /api/comment by postId
    public Map<Long, CommentResponseDto.CommentTreeDTO> getCommentTree(Long postId){
         List<Comment> comments = commentRepository.findByPostIdOrderByAsc(postId);
         return  CommentDtoConverter.convertToTreeDtoMap(comments);
    }

    // GET /api/comment/id
    public CommentResponseDto.CommentTreeDTO getCommentById(Long commentId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.COMMENT_NOT_FOUND));
        if (comment.getStatus() == Scope.PRIVATE) {
            throw new ErrorHandler(ErrorStatus.COMMENT_IS_DELETED);
        }
        return converToCommentTreeDTO(comment);

    }

    // PATCH /api/comment/id
    public CommentResponseDto.CommentTreeDTO updateComment(CommentRequestDto.CommentUpdateRequest requestDto){
        Comment comment = commentRepository.findById(requestDto.getCommentId())
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.COMMENT_NOT_FOUND));

        if(!((comment.getMember().getMemberId()).equals(requestDto.getMemberId()))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        if (comment.getStatus() == Scope.PRIVATE) {
            throw new ErrorHandler(ErrorStatus.COMMENT_IS_DELETED);
        }
        comment.updateContent(requestDto.getContent());
        return  converToCommentTreeDTO(comment);
    }

    // DELETE /api/comment
    public DeleteStatus deleteComment(String memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.COMMENT_NOT_FOUND));

        if(!(memberId.equals(comment.getMember().getMemberId()))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);

        boolean allChildrenPrivate = comment.getChildComments().stream().allMatch(
                child -> child.getDeleteStatus() == DeleteStatus.DELETE
        );

        // 자식 댓글이 없거나, 자식 댓글의 DELETE STATUS가 모두 DELETE인 경우
        if (comment.getChildComments().isEmpty() || allChildrenPrivate) {
            comment.updateDeleteStatus(DeleteStatus.DELETE);
            comment.updateStatus(Scope.PRIVATE);
            commentRepository.save(comment);
            updateParentDeleteStatus(comment);
            return DeleteStatus.DELETE;
        } else {
            // 자식 댓글이 하나라도 살아있으면
            comment.updateDeleteStatus(DeleteStatus.DEAD);
            comment.updateStatus(Scope.PRIVATE);
            commentRepository.save(comment);
            return DeleteStatus.DEAD;
        }
    }
    private void updateParentDeleteStatus(Comment comment) {
        Comment parent = comment.getParent();
        while (parent != null) {
            List<Comment> childComments = parent.getChildComments();

            List<Comment> aliveChilds = childComments.stream()
                    .filter(child -> child.getDeleteStatus() != DeleteStatus.DELETE)
                    .collect(Collectors.toList());

            boolean isChildAlive = childComments.stream()
                    .allMatch(child -> child.getDeleteStatus() == DeleteStatus.DELETE);
            System.out.println(aliveChilds);

            // 자식이 다 DELETE, 자기도 DEAD
            if(isChildAlive &&
                    parent.getDeleteStatus() == DeleteStatus.DEAD){
                parent.updateDeleteStatus(DeleteStatus.DELETE);
                commentRepository.save(parent);
                comment = parent;
                parent = comment.getParent();
            }
            else {
                break;
            }
        }
    }

    // SAVE COMMENT 메서드
    public Comment saveComment(CommentRequestDto.CommentBasicRequest requestDto){
        Member member = getMember(requestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        Post post = getPost(requestDto.getPostId());
        if(post == null)  throw new ErrorHandler(ErrorStatus.POST_NOT_FOUND);

        Comment parentComment = null;
        // 자식 댓글일 경우
        if (requestDto.getParentId() != null) {
            parentComment = getComment(requestDto.getParentId());
            if (parentComment == null || !parentComment.getPost().equals(post)) {
                throw new ErrorHandler(ErrorStatus.PARENT_COMMENT_AND_POST_NOT_FOUND);
            }
        }

        Comment comment = Comment.builder()
                .member(member)
                .post(post)
                .status(requestDto.getStatus())
                .content(requestDto.getContent())
                .build();
        // 자식 댓글인 경우
        if(parentComment != null){
            comment.updateParent(parentComment);
        }
        commentRepository.save(comment);
       return comment;
    }

    // GET member 메서드
    public Member getMember(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member = null;
        if (memberOptional.isPresent()) {
            member = memberOptional.get();
        }
        return member;
    }

    // GET post 메서드
    public Post getPost(Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        Post post = null;
        if(postOptional.isPresent()){
            post = postOptional.get();
        }
        return post;
    }

    // GET parent comment 메서드
    public Comment getComment(Long commentId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        Comment comment = null;
        if (commentOptional.isPresent()) {
            comment = commentOptional.get();
        }
        return comment;
    }


}
