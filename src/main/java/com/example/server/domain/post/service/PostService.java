package com.example.server.domain.post.service;

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
import com.example.server.domain.post.repository.PostRepository;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.domain.ticket.domain.Ticket;
import com.example.server.domain.ticket.dto.TicketRequestDto;
import com.example.server.domain.ticket.repository.TicketRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    @PersistenceContext
    private EntityManager entityManager;

    // POST api/post/
    @Transactional
    public PostResponseDto.GetPostResponseDto uploadPost(PostRequestDto.UploadPostRequestDto requestDto) {
        PostRequestDto.CommonPostRequestDto postRequestDto = requestDto.getPostRequest();
        Member member = getMember(postRequestDto.getMemberId());
        if (member == null) throw new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND);
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
        return PostDtoConverter.convertToGetResponseDto(post);
    }

    // GET api/post
    public PostResponseDto.GetPostResponseDto getPost(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        return PostDtoConverter.convertToGetResponseDto(post);
    }

    // GET api/post
    public List<PostResponseDto.GetPostResponseDto> getAllPost(Integer page, Integer size){
        // 둘다 0일때 => 변수 입력 안받음
        if(page==0 && size==0){
            List<Post> postList = postRepository.findAll();
            return PostDtoConverter.convertToPostListResponseDto(postList);
        }
        else {
            PageRequest pageable = PageRequest.of(page, size);
            List<Post> postList = postRepository.findAll(pageable).getContent();
            return PostDtoConverter.convertToPostListResponseDto(postList);
        }
    }

    public List<PostResponseDto.GetPostResponseDto> getAllMemberPost(String memberId,Integer page, Integer size){
        Optional<Member> member = memberRepository.findByMemberId(memberId);
        // 둘다 0일때 => 변수 입력 안받음
        if(page==0 && size==0){
            List<Post> postList = postRepository.findAllByMember(member.get());
            return PostDtoConverter.convertToPostListResponseDto(postList);
        }
        else {
            Pageable pageable = PageRequest.of(page, size);
            List<Post> postList = postRepository.findAllByMember(member.get(), pageable).getContent();
            return PostDtoConverter.convertToPostListResponseDto(postList);
        }

    }


    // DELETE api/post
    public PostResponseDto.DeletePostResultResponseDto deletePost(Long postId, String memberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ErrorHandler(ErrorStatus.POST_NOT_FOUND));
        if(!((post.getMember().getEmail()).equals(memberId))) throw new ErrorHandler(ErrorStatus.NO_PERMISSION__FOR_POST);
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
        return PostDtoConverter.convertToGetResponseDto(post);
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




}
