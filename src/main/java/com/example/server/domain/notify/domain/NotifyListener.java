package com.example.server.domain.notify.domain;

import com.example.server.domain.notify.dto.NotifyDto;
import com.example.server.domain.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class NotifyListener {

    private final NotifyService notifyService;

    @TransactionalEventListener
    @Async
    public void handleNotification(NotifyDto.NotifyRequestDto requestDto) {
        notifyService.sendNotify(requestDto.getReceiver(), requestDto);
    }
}
