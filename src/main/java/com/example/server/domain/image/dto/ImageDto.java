package com.example.server.domain.image.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ImageDto {
    private String accessUri;
    private String authenticateId;
    private String imgUrl;
}
