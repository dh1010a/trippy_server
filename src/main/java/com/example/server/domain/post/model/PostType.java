package com.example.server.domain.post.model;

import com.example.server.domain.member.model.Scope;

public enum PostType {
    POST, OOTD;

    public static PostType fromName(String type) {
        return PostType.valueOf(type.toUpperCase());
    }
}
