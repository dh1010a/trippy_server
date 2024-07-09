package com.example.server.domain.notify.repository;

import com.example.server.domain.notify.domain.Notify;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyRepository extends JpaRepository<Notify, Long>{
}
