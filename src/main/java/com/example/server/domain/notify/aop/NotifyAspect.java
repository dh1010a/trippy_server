//package com.example.server.domain.notify.aop;
//
//import com.example.server.domain.notify.service.NotifyService;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Slf4j
//@Component
//@EnableAsync
//public class NotifyAspect {
//    // yataRequestService  createRequest method pointcut
//    private final NotifyService notifyService;
//
//    public NotifyAspect(NotifyService notifyService) {
//        this.notifyService = notifyService;
//    }
//
//
//    @Pointcut("@annotation(com.example.server.domain.notify.annotation.NeedNotify)")
//    public void annotationPointcut() {
//    }
//
//    @Async
//    @AfterReturning(pointcut = "annotationPointcut()", returning = "result")
//    public void checkValue(JoinPoint joinPoint, Object result) throws Throwable {
//        NotifyInfo notifyProxy = (NotifyInfo) result;
//        notifyService.send(
//                notifyProxy.getReceiver(),
//                notifyProxy.getNotificationType(),
//                NotifyMessage.getMessage(),
//                "/api/member/" + (notifyProxy.getGoUrlId())
//        );
//        log.info("result = {}", result);
//    }
//}
