package com.example.server.domain.post.service;

import com.example.server.domain.post.domain.Tag;
import com.example.server.domain.post.repository.TagRepository;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository tagRepository;

    public void deleteTag(Tag tag){
        if(tagRepository.existsById(tag.getId())) tagRepository.delete(tag);
        else  throw new ErrorHandler(ErrorStatus._BAD_REQUEST);
    }

}
