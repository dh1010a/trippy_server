package com.example.server.domain.image.model;

import static java.util.Locale.ENGLISH;

public enum ImageType {
    POST, PROFILE, BLOG;

    public static ImageType fromName(String type) {
        return ImageType.valueOf(type.toUpperCase(ENGLISH));
    }
}