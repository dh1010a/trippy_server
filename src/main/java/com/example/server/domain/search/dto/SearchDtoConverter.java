package com.example.server.domain.search.dto;

import com.example.server.domain.image.domain.Image;
import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.dto.MemberResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchDtoConverter {

    public static List<SearchResponseDto.SearchMemberDto> convertToSearchMemberDto(List<Member> members) {
        return members.stream()
                .map(member -> {
                    List<Image> images = member.getImages();
                    Image profileImage = images.stream().filter(Image::isProfileImage).findAny().orElse(null);
                    Image blogTitleImage = images.stream().filter(Image::isBlogTitleImage).findAny().orElse(null);

                    return SearchResponseDto.SearchMemberDto.builder()
                            .profileImgUrl(profileImage != null ? profileImage.getAccessUri() : null)
                            .blogTitleImgUrl(blogTitleImage != null ? blogTitleImage.getAccessUri() : null)
                            .memberId(member.getMemberId())
                            .blogName(member.getBlogName())
                            .blogIntroduction(member.getBlogIntroduce() != null ? member.getBlogIntroduce() : "")
                            .nickName(member.getNickName())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
