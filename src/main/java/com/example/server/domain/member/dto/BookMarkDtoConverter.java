package com.example.server.domain.member.dto;

import com.example.server.domain.member.domain.BookMark;

public class BookMarkDtoConverter {

    public static BookMarkResponseDto.BookMarkBasicResponse convertToBookMarkDto(BookMark bookMark){
        return BookMarkResponseDto.BookMarkBasicResponse.builder().
                memberIdx(bookMark.getMember().getIdx())
                .postId(bookMark.getPost().getId())
                .bookMarkId(bookMark.getId()).
                build();
    }
}
