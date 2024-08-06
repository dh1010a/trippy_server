package com.example.server.domain.image.service;

import com.example.server.domain.image.dto.ImageDto;
import com.example.server.domain.image.dto.ImageResponseDto;
import com.example.server.domain.image.dto.ImageResponseDto.UploadResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ImageService {

    public UploadResponseDto uploadImg(MultipartFile file, String memberId, HttpSession session) throws Exception;

    public MultipartFile downloadImg(Long imageIdx, Long memberIdx) throws Exception;

    public void deleteImg(Long imageIdx) throws Exception;

    public void deleteImg(ImageDto imageDto) throws Exception;

    public void removeNewFile(File targetFile) ;

}
