package com.example.server.domain.image.service;

import com.example.server.domain.image.dto.ImageResponseDto.UploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ImageService {

    public UploadResponseDto uploadProfileImg(MultipartFile file, String memberId) throws Exception;
    public UploadResponseDto uploadImg(MultipartFile file, String memberId) throws Exception;

    public MultipartFile downloadImg(Long imageIdx, Long memberIdx) throws Exception;

    public void deleteImg(Long imageIdx) throws Exception;

    public void removeNewFile(File targetFile) ;

}
