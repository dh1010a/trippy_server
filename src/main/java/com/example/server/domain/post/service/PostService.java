package com.example.server.domain.post.service;

import com.example.server.domain.country.service.CountryService;
import com.example.server.domain.follow.repository.MemberFollowRepository;
import com.example.server.domain.image.domain.Image;
import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.model.ImageType;
import com.example.server.domain.image.repository.ImageRepository;
import com.example.server.domain.member.domain.Member;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final MemberFollowRepository memberFollowRepository;

    private final CountryService countryService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String VIEW_COOKIE_NAME = "View_Count";

    // POST api/post/
    public PostResponseDto.GetPostResponseDto uploadPost(PostRequestDto.UploadPostRequestDto requestDto) {
        Member member = getMemberById(requestDto.getPostRequest().getMemberId());
        Post post = savePost(requestDto.getPostRequest());

        if (requestDto.getPostRequest().getImages() != null) {
            List<Image> images = saveImages(requestDto.getPostRequest(), post);
            post.updateImages(images);
        }

        if (requestDto.getPostRequest().getTags() != null) {
            List<Tag> tags = saveTags(requestDto, post);
            post.updateTags(tags);
        }

        Ticket ticket = saveTicket(requestDto.getTicketRequest());
        savePostAndTicket(post, ticket);

        return PostDtoConverter.convertToGetResponseDto(post, member);
    }

    // GET api/post
    public PostResponseDto.GetPostResponseDto getPost(Long postId, String memberId, HttpServletRequest request, HttpServletResponse response) {
        Post post = getPostById(postId);

        if (post.getPostType() == PostType.OOTD) {
            throw new ErrorHandler(ErrorStatus.POST_TYPE_ERROR);
        } else {
            addViewCount(request,postId);
        }

        Member member = getMemberById(memberId);
        return PostDtoConverter.convertToGetResponseDto(post, member);
    }

    public void addViewCount(HttpServletRequest request, Long postId) {
        HttpSession session = request.getSession();
        Post post = getPostById(postId);

        // 세션에서 방문한 게시물 ID 목록 가져오기
        List<Long> viewedPosts = (List<Long>) session.getAttribute("viewedPosts");

        if (viewedPosts == null) {
            viewedPosts = new ArrayList<>();
        }

        // 현재 게시물이 이미 방문한 게시물 목록에 있는지 확인
        if (!viewedPosts.contains(postId)) {
            viewedPosts.add(postId);
            post.addViewCount();
        }

        // 세션에 방문한 게시물 ID 목록 저장
        session.setAttribute("viewedPosts", viewedPosts);
    }


    // GET api/post < 게시물 전체 불러 오기 >
    public List<PostResponseDto.GetPostResponseDto> getAllPost(Integer page, String memberId, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);

        List<Long> followingList = getFollowingList(member);

        Pageable pageable = getPageable(page, size, orderType);
        List<Post> postList = getPostsByOrderType(orderType, pageable, followingList);

        return PostDtoConverter.convertToPostListResponseDto(postList, member);
    }

    public List<PostResponseDto.GetPostResponseDto> getAllMyPost(String loginMemberId, Integer page, Integer size, OrderType orderType) {
        Member loginMember = getMemberById(loginMemberId);
        Pageable pageable = getPageable(page, size, orderType);

        List<Post> postList = getMyPostsByOrderType(orderType, pageable, loginMember);
        return PostDtoConverter.convertToPostListResponseDto(postList, loginMember);
    }

    // 멤버별 게시물
    public List<PostResponseDto.GetPostResponseDto> getAllMemberPost(String memberId, String loginMemberId, Integer page, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);
        Member loginMember = getMemberById(loginMemberId);
        List<Long> followingList = getFollowingList(member);
        Pageable pageable = getPageable(page, size, orderType);

        List<Post> postList = getPostsByOrderTypeAndMember(orderType, pageable, member, followingList);
        return PostDtoConverter.convertToPostListResponseDto(postList, loginMember);
    }

    public Pageable getPageable(Integer page, Integer size, OrderType orderType) {
        if (page == 0 && size == 0) {
            return Pageable.unpaged();
        } else {
            return PageRequest.of(page, size, getSortByOrderType(orderType));
        }
    }


    // 전체 게시물
    public List<Post> getPostsByOrderType(OrderType orderType, Pageable pageable, List<Long> followingList) {
        return postRepository.findAllByPostTypeWithScope(PostType.POST,followingList, pageable).getContent();
    }

    // 멤버별 게시물
    public List<Post> getPostsByOrderTypeANdMember(OrderType orderType, Pageable pageable, Member member, List<Long> followingList) {
        return postRepository.findAllByMemberAndPostType(PostType.POST, member,followingList, pageable).getContent();
    }

    // 내 게시물
    public List<Post> getMyPostsByOrderType(OrderType orderType, Pageable pageable, Member member) {
        return postRepository.findMyPostsWithScore(member,PostType.POST, pageable).getContent();
    }

    // 멤버 게시물
    public List<Post> getPostsByOrderTypeAndMember(OrderType orderType, Pageable pageable, Member member, List<Long> followingList) {
        return postRepository.findAllByMemberAndPostType( PostType.POST, member, followingList, pageable).getContent();
    }

    public Sort getSortByOrderType(OrderType orderType) {
        switch (orderType) {
            case VIEW:
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case LIKE:
                return Sort.by(Sort.Direction.DESC, "score");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    public PostResponseDto.DeletePostResultResponseDto deletePost(Long postId, String memberId) {
        Post post = getPostById(postId);
        validatePostOwnership(post, memberId);

        postRepository.delete(post);
        return PostDtoConverter.convertToDeletePostDto(postId);
    }

    public Object updatePost(PostRequestDto.UpdatePostRequestDto requestDto) {
        Post post = getPostById(requestDto.getId());
        Member member = getMemberById(requestDto.getMemberId());

        validatePostOwnership(post, member.getMemberId());

        updateTagsAndImages(post, requestDto.getTags(), requestDto.getImages());
        post.updatePost(requestDto);

        if (post.getPostType().equals(PostType.OOTD)) {
            return PostDtoConverter.convertToOotdResponseDto(post, member);
        }

        return PostDtoConverter.convertToGetResponseDto(post, member);
    }

    // 팔로잉 게시물 -> POST
    public List<PostResponseDto.GetPostResponseDto> getPostsFromFollowedMembers(String memberId, PostType postType, Integer page, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);
        Pageable pageable = getPageable(page, size, orderType);

        List<Long> followingMemberIds = getFollowingList(member);

        List<Post> posts = postRepository.findByMemberIdxInAndPostType(followingMemberIds, postType, pageable).getContent();

        return PostDtoConverter.convertToPostListResponseDto(posts, member);
    }

    // 팔로잉 게시물 -> OOTD
    public List<PostResponseDto.GetOotdPostResponseDto> getOotdsFromFollowedMembers(String memberId, PostType postType, Integer page, Integer size, OrderType orderType) {
        Member member = getMemberById(memberId);
        Pageable pageable = getPageable(page, size, orderType);

        List<Long> followingMemberIds = getFollowingList(member);

        List<Post> posts = postRepository.findByMemberIdxInAndPostType(followingMemberIds, postType, pageable).getContent();

        return PostDtoConverter.convertToOOTDListResponseDto(posts, member);
    }

    public Post savePost(PostRequestDto.CommonPostRequestDto requestDto) {
        Member member = getMemberById(requestDto.getMemberId());

        if (member == null) {
            throw new ErrorHandler(ErrorStatus.MEMBER_EMAIL_ALREADY_EXIST);
        }

        return Post.builder()
                .member(member)
                .title(requestDto.getTitle())
                .body(requestDto.getBody())
                .postType(requestDto.getPostType())
                .location(requestDto.getLocation())
                .build();
    }

    private Ticket saveTicket(TicketRequestDto.UploadTicketRequestDto requestDto) {
        Ticket ticket = Ticket.builder()
                .departure(requestDto.getDeparture())
                .destination(requestDto.getDestination())
                .memberNum(requestDto.getMemberNum())
                .startDate(requestDto.getStartDate())
                .departureCode(requestDto.getDepartureCode())
                .destinationCode(requestDto.getDestinationCode())
                .endDate(requestDto.getEndDate())
                .ticketColor(requestDto.getTicketColor())
                .transport(requestDto.getTransport())
                .build();

        if (requestDto.getImage() != null) {
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

    private List<Tag> saveTags(PostRequestDto.UploadPostRequestDto requestDto, Post post) {
        List<Tag> collect = new ArrayList<>();
        // 국가와 도시 태그 추가
        Tag countryTag = Tag.builder()
                .name(requestDto.getTicketRequest().getDestination())
                .post(post)
                .build();
        tagRepository.save(countryTag);
        collect.add(countryTag);
        Tag cityTag = Tag.builder()
                .name(countryService.getCountryByLocation(requestDto.getTicketRequest().getDestination()).getCountryNm())
                .post(post)
                .build();
        tagRepository.save(cityTag);
        collect.add(cityTag);


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

    public List<Image> saveImages(PostRequestDto.CommonPostRequestDto requestDto, Post post) {
        return requestDto.getImages().stream()
                .map(imageDto -> {
                    Image image = Image.builder()
                            .imgUrl(imageDto.getImgUrl())
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

    @Transactional
    protected void savePostAndTicket(Post post, Ticket ticket) {
        ticketRepository.save(ticket);
        post.updateTicket(ticket);
        postRepository.save(post);
    }

    private void updateTagsAndImages(Post post, List<String> newTagNames, List<ImageDto> newImagesDto) {
        List<Tag> originalTags = tagRepository.findAllByPost(post);
        List<Image> originalImages = imageRepository.findAllByPostAndImageType(post, ImageType.POST);

        newTagNames.forEach(newTagName -> {
            if (originalTags.stream().noneMatch(tag -> tag.getName().equals(newTagName))) {
                Tag newTag = Tag.builder()
                        .name(newTagName)
                        .post(post)
                        .build();
                tagRepository.save(newTag);
            } else {
                originalTags.removeIf(tag -> tag.getName().equals(newTagName));
            }
        });

        newImagesDto.forEach(newImageDto -> {
            if (originalImages.stream().noneMatch(image -> image.getImgUrl().equals(newImageDto.getImgUrl()))) {
                Image newImage = Image.builder()
                        .imgUrl(newImageDto.getImgUrl())
                        .authenticateId(newImageDto.getAuthenticateId())
                        .accessUri(newImageDto.getAccessUri())
                        .imageType(ImageType.POST)
                        .post(post)
                        .build();
                imageRepository.save(newImage);
            } else {
                originalImages.removeIf(image -> image.getImgUrl().equals(newImageDto.getImgUrl()));
            }
        });

        originalTags.forEach(tagRepository::delete);
        originalImages.forEach(imageRepository::delete);

        tagRepository.flush();
        imageRepository.flush();
    }

    // 전체 게시물 개수
    public long getTotalCount(String memberId, PostType type) {
        Member member = getMemberById(memberId);
        List<Long> followingList = getFollowingList(member);

        return postRepository.countByPostTypeWithScope(type, followingList);
    }

    // 특정 멤버 게시물 게수
    public long getTotalCountByMember(String loginMemberId, String memberId, PostType type) {
        Member loginMember = getMemberById(loginMemberId);
        Member targetMember = getMemberById(memberId);
        List<Long> followingList = getFollowingList(loginMember);
        return postRepository.countByMemberAndPostTypeWithScope(targetMember, type, followingList);
    }

    // 내 게시물 개수
    public long getTotalCountMy(String loginMemberId, PostType type) {
        Member loginMember = getMemberById(loginMemberId);
        return postRepository.countByMemberAndPostType(loginMember,type);
    }

    // 팔로잉 개수
    public long getFollowingCount(String loginMemberId, PostType type) {
        Member loginMember = getMemberById(loginMemberId);
        List<Long> followingList =getFollowingList(loginMember);
        return postRepository.countByMemberIdxInAndPostType(followingList, type);
    }

    public Member getMemberById(String memberId) {
        if ("anonymousUser".equals(memberId)) {
            return null;
        }
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
    }

    public List<Long> getFollowingList(Member member) {
        return member == null ? new ArrayList<>() : memberFollowRepository.findFollowingList(member.getIdx());
    }

    private void validatePostOwnership(Post post, String memberId) {
        if (!post.getMember().getMemberId().equals(memberId)) {
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
        }
    }
}
