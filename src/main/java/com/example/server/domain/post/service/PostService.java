package com.example.server.domain.post.service;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.domain.post.domain.Post;
import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.dto.PostDtoConverter;
import com.example.server.domain.post.dto.PostRequestDto;
import com.example.server.domain.post.dto.PostResponseDto;
import com.example.server.domain.post.model.OrderType;
import com.example.server.domain.post.model.PostType;
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.domain.ticket.repository.TicketRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.servlet.http.Cookie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.server.domain.member.model.Role.ROLE_MEMBER;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String VIEW_COOKIE_NAME= "View_Count";

    // POST api/post/
    @Transactional
    public PostResponseDto.GetPostResponseDto uploadPost(PostRequestDto.UploadPostRequestDto requestDto) {
        PostRequestDto.CommonPostRequestDto postRequestDto = requestDto.getPostRequest();
        Member member = Optional.ofNullable(getMember(postRequestDto.getMemberId())).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        TicketRequestDto.UploadTicketRequestDto ticketRequestDto = requestDto.getTicketRequest();
        Post post = savePost(postRequestDto);
        System.out.println(requestDto.toString());
        if (postRequestDto.getImages() != null) {
            List<Image> images = saveImages(postRequestDto,post);
            post.updateImages(images);
        }
        if(postRequestDto.getTags() != null) {
            List<Tag> tags = saveTags(postRequestDto, post);
            post.updateTags(tags);
        }

        Ticket ticket = saveTicket(ticketRequestDto);
        savePostAndTicketAndAll(post,ticket);
        return PostDtoConverter.convertToGetResponseDto(post,member);
    }

    // GET api/post
    public PostResponseDto.GetPostResponseDto getPost(Long postId, String memberId, HttpServletRequest request, HttpServletResponse response){
        // 조회수 증가
       // response.addCookie(cookie);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if(post.getPostType() == PostType.OOTD) {
            throw new ErrorHandler(ErrorStatus.POST_TYPE_ERROR);
        }
        else  addViewCount(request,response, postId);

        Member member = !memberId.equals("anonymousUser") ? Optional.ofNullable(getMember(memberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)) : null;
        return PostDtoConverter.convertToGetResponseDto(post,member);
    }

    // GET api/post
    public List<PostResponseDto.GetPostResponseDto> getAllPost(Integer page, String memberId, Integer size, OrderType orderType){

        Member member = !memberId.equals("anonymousUser") ? Optional.ofNullable(getMember(memberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND)) : null;

        // 둘다 0일때 => 변수 입력 안받음
        Sort sort = getSortByOrderType(orderType);

        if(page==0 && size==0){
            List<Post> postList = postRepository.findAllByPostType(PostType.POST,sort);
            return PostDtoConverter.convertToPostListResponseDto(postList, member);
        }
        else {
            PageRequest pageable = PageRequest.of(page, size,sort);
            List<Post> postList = postRepository.findAllByPostType(PostType.POST,pageable).getContent();
            return PostDtoConverter.convertToPostListResponseDto(postList,member);
        }
    }
    public List<PostResponseDto.GetPostResponseDto> getAllMemberPost(String memberId,String loginMemberId, Integer page, Integer size, OrderType orderType){
        Member member = Optional.ofNullable(getMember(memberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member loginMember = Optional.ofNullable(getMember(loginMemberId)).orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Sort sort = getSortByOrderType(orderType);
        // 둘다 0일때 => 변수 입력 안받음
        if(page==0 && size==0){
            List<Post> postList = postRepository.findAllByMemberAndPostType(member,PostType.POST,sort);
            return PostDtoConverter.convertToPostListResponseDto(postList,loginMember);
        }
        else {
            PageRequest pageable = PageRequest.of(page, size,sort);
            List<Post> postList = postRepository.findAllByMemberAndPostType(member,PostType.POST, pageable).getContent();
            return PostDtoConverter.convertToPostListResponseDto(postList,loginMember);
        }

    }

    public Sort getSortByOrderType(OrderType orderType) {
        switch (orderType) {
            case LIKE:
                return Sort.by(Sort.Direction.DESC, "likes.size");
            case VIEW:
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case LATEST:
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }



    // DELETE api/post
    public PostResponseDto.DeletePostResultResponseDto deletePost(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if(!((post.getMember().getMemberId()).equals(memberId))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        postRepository.delete(post);
        return PostDtoConverter.convertToDeletePostDto(postId);
    }

    // PATCH api/post
    public PostResponseDto.GetPostResponseDto updatePost(PostRequestDto.UpdatePostRequestDto requestDto) {
        Post post = postRepository.findById(requestDto.getId()).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        Member member =  getMember(requestDto.getMemberId());
        if(!((post.getMember().getIdx()).equals(member.getIdx()))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        updateTagsAndImages(post,requestDto.getTags(),requestDto.getImages());
        post.updatePost(requestDto);
        return PostDtoConverter.convertToGetResponseDto(post,member);
    }

    // GET member 메서드
    public Member getMember(String memberId) {
        if ("anonymousUser".equals(memberId)) {
            return null;
        }
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    // POST 빌드 메서드
    public Post savePost(PostRequestDto.CommonPostRequestDto requestDto){
        Member member = getMember(requestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);

        Post post = Post.builder()
                .member(member)
                .title(requestDto.getTitle())
                .body(requestDto.getBody())
                .postType(requestDto.getPostType())
                .location(requestDto.getLocation())
                .build();
        return post;
    }

    public Ticket saveTicket(TicketRequestDto.UploadTicketRequestDto requestDto){
        Ticket ticket = Ticket.builder()
                .departure(requestDto.getDeparture())
                .destination(requestDto.getDestination())
                .memberNum(requestDto.getMemberNum())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .ticketColor(requestDto.getTicketColor())
                .transport(requestDto.getTransport())
                .build();

        if(requestDto.getImage()!=null){
            Image ticketImage = Image.builder()
                    .imageType(ImageType.TICKET)
                    .authenticateId(requestDto.getImage().getAuthenticateId())
                    .accessUri(requestDto.getImage().getAccessUri())
                    .imgUrl(requestDto.getImage().getImgUrl())
                    .build();

            imageRepository.save(ticketImage);
            ticket.updateImage(ticketImage);
        }
        return ticket;
    }

    // TAG 엔티티 저장 메서드
    public List<Tag> saveTags(PostRequestDto.CommonPostRequestDto requestDto,Post post) {
        return requestDto.getTags().stream()
                .map(tagName -> {

                    Tag tag = Tag.builder().name(tagName)
                            .post(post)
                            .build();
                    tagRepository.save(tag);
                    return tag;
                })
                .collect(Collectors.toList());
    }

    // IMAGE 저장 메서드 for [POST api/post/]
    public List<Image> saveImages(PostRequestDto.CommonPostRequestDto requestDto,Post post) {

        return requestDto.getImages().stream()
                .map(imageDto -> {
                    Image image = Image.builder().imgUrl(imageDto.getImgUrl())
                            .accessUri(imageDto.getAccessUri())
                            .authenticateId(imageDto.getAuthenticateId())
                            .post(post)
                            .imageType(ImageType.POST)
                            .build();
                    imageRepository.save(image);
                    return image;
                })
                .collect(Collectors.toList());
    }

    // POST 저장 메서드 for [POST api/post/]
    @Transactional
    public void savePostAndTicketAndAll(Post post, Ticket ticket) {
        ticketRepository.save(ticket);
        post.updateTicket(ticket);
        postRepository.save(post);
    }

    public void updateTagsAndImages(Post post, List<String> newTagNames, List<ImageDto> newImagesDto) {
        // 기존 태그 및 이미지 가져오기
        List<Tag> originalTags = tagRepository.findAllByPost(post);
//        List<Image> originalImages = imageRepository.findAllByPost(post);
        List<Image> originalImages = imageRepository.findAllByPostAndImageType(post,ImageType.POST);

        // 새로운 태그 추가
        for (String newTagName : newTagNames) {
            // 만약 기존 TAG에 현재 Tag name 없으면 Tag 새로 추가
            if (originalTags.stream().noneMatch(tag -> tag.getName().equals(newTagName))) {
                Tag newTag = Tag.builder()
                        .name(newTagName)
                        .post(post)
                        .build();
                tagRepository.save(newTag);
            }
            // 기존 TAG에 현재 Tag name 있으면 -> 변동 없다는 뜻
            else {
                originalTags.removeIf(tag -> tag.getName().equals(newTagName));
            }
        }
        // 새로운 이미지 추가
        for (ImageDto newImageDto : newImagesDto) {
            if (originalImages.stream().noneMatch(image -> image.getImgUrl().equals(newImageDto.getImgUrl()))) {
                Image newImage = Image.builder()
                        .imgUrl(newImageDto.getImgUrl())
                        .authenticateId(newImageDto.getAuthenticateId())
                        .accessUri(newImageDto.getAccessUri())
                        .imageType(ImageType.POST)
                        .post(post)
                        .build();
                imageRepository.save(newImage);
            }
            else {
                originalImages.removeIf(image -> image.getImgUrl().equals(newImageDto.getImgUrl()));
            }
        }

        // 기존 태그 삭제 (이제 사용 되지 않는)
        for (Tag tag : originalTags) {
            tagRepository.delete(tag);
        }

        // 기존 이미지 삭제 (이제 사용 되지 않는)
        for (Image image : originalImages) {
            imageRepository.delete(image);
        }
        tagRepository.flush();
        imageRepository.flush();
    }

    public long getTotalCount(PostType type){
        return postRepository.countByPostType(type);
    }

    public long getTotalCountByMember(String memberId, PostType type){
        Member member = getMember(memberId);
        return postRepository.countByMemberAndPostType(member,type);
    }

    // 조회수 증가 메서드
    public void addViewCount(HttpServletRequest request, HttpServletResponse response, Long postId) {
        Cookie[] cookies = request.getCookies();
        boolean isNewCookie = true;
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("visit_cookie".equals(cookie.getName())) {
                    if ( !cookie.getValue().contains(postId.toString())) {
                        cookie.setValue(cookie.getValue() + "_" + postId.toString());
                        cookie.setMaxAge(60 * 60 * 2); // (2시간)
                        response.addCookie(cookie);
                        post.addViewCount();
                    }
                    isNewCookie = false;
                    break;
                }
            }
        }

        if (isNewCookie) {
            Cookie newCookie = new Cookie("visit_cookie", postId.toString());
            newCookie.setMaxAge(60 * 60 * 2); // 쿠키 시간 설정 (2시간)
            response.addCookie(newCookie);
            post.addViewCount();
        }
    }




}