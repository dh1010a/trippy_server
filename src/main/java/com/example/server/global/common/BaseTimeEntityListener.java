package com.example.server.global.common;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class BaseTimeEntityListener {

    @PrePersist // DB에 해당 테이블을 insert 연산을 실행할 때, 같이 실행된다.
    public void setCreatedAtAndUpdatedAt(Object o){
        if(o instanceof BaseTimeEntity){
            ((BaseTimeEntity) o).setCreatedAt(LocalDateTime.now());
            ((BaseTimeEntity) o).setLastModifiedAt(LocalDateTime.now());
        }
    }

    @PreUpdate // DB에 해당 테이블 update 연산이 실행될 때, 같이 실행된다.
    public void preUpdate(Object o){
        if(o instanceof BaseTimeEntity){
            ((BaseTimeEntity) o).setLastModifiedAt(LocalDateTime.now());
        }
    }
}
