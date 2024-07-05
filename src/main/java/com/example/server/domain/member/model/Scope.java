package com.example.server.domain.member.model;

public enum Scope {
    PUBLIC("public", "전체공개"),
    PRIVATE("private", "비공개"),
    PROTECTED("protected", "팔로워공개");

    private final String key;
    private final String title;

    Scope(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public static Scope fromName(String type) {
        return Scope.valueOf(type.toUpperCase());
    }

}
