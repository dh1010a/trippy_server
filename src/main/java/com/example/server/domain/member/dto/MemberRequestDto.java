package com.example.server.domain.member.dto;

import lombok.Builder;
import lombok.Data;

public class MemberRequestDto {

        @Builder
        @Data
        public static class CreateMemberRequestDto {
            private String memberId;
            private String password;
            private String name;
            private String email;
            private String nickName;
            private String phone;
            private String gender;
        }
}
