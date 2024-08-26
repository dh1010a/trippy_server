package com.example.server.domain.bookmark.dto;

import com.example.server.domain.bookmark.domain.BookMark;

public class BookMarkDtoConverter {

    public static BookMarkResponseDto.BookMarkBasicResponse convertToBookMarkDto(BookMark bookMark){
        return BookMarkResponseDto.BookMarkBasicResponse.builder().
                memberIdx(bookMark.getMember().getIdx())
                .postId(bookMark.getPost().getId())
                .bookMarkId(bookMark.getId()).
                build();
    }
}
