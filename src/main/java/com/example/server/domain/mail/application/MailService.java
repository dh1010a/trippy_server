package com.example.server.domain.mail.application;

import com.example.server.domain.mail.converter.MailDtoConverter;
import com.example.server.domain.mail.dto.MailResponseDto;
import com.example.server.domain.mail.dto.MailResponseDto.CheckMailResponseDto;
import com.example.server.domain.member.service.MemberService;
import com.example.server.global.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    private final RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    private String username;

    private static final int CODE_LENGTH = 6;

    //mail을 어디서 보내는지, 어디로 보내는지 , 인증 번호를 html 형식으로 어떻게 보내는지 작성합니다.
    public CheckMailResponseDto sendEmail(String email) {
        String authNumber = makeRandomNumber();
        String toMail = email;
        String title = authNumber + "은(는) 회원님의 Trippy 이메일 인증번호 입니다."; // 이메일 제목
        String content =
                " 회원님의 Trippy 서비스 이메일 인증 요청을 받았습니다." + 	//html 형식으로 작성
                        "<br><br>" +
                        "인증 번호는 " + authNumber + " 입니다." +
                        "<br>" +
                        "인증번호를 정확히 입력해주세요."; //이메일 내용 삽입
        send(username, toMail, title, content);
        log.info("요청한 {} 주소로 이메일 전송이 완료되었습니다.", toMail);
        redisUtil.setDataExpire(authNumber,toMail,60*5L);
        return MailDtoConverter.convertCheckMailResultToDto(true);
    }

    public CheckMailResponseDto checkEmail(String email, String authNumber) {
        String data = redisUtil.getData(authNumber);
        if (data == null || !data.equals(email)) {
            log.info("인증에 실패하였습니다. email = {}", email);
            return MailDtoConverter.convertCheckMailResultToDto(false);
        }
        log.info("인증에 성공하였습니다. email = {}", email);
        return MailDtoConverter.convertCheckMailResultToDto(true);
    }

    public void finishCheckEmail(String authNumber) {
        redisUtil.deleteData(authNumber);
    }

    //임의의 6자리 양수를 반환합니다.
    private String makeRandomNumber() {
        Random r = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for(int i = 0; i < CODE_LENGTH; i++) {
            randomNumber.append(r.nextInt(10));
        }

        return randomNumber.toString();
    }

    //이메일을 전송합니다.
    private void send(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            helper.setFrom(setFrom);//이메일의 발신자 주소 설정
            helper.setTo(toMail);//이메일의 수신자 주소 설정
            helper.setSubject(title);//이메일의 제목을 설정
            helper.setText(content,true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
            mailSender.send(message);
        } catch (MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
            // 이러한 경우 MessagingException이 발생
            e.printStackTrace();//e.printStackTrace()는 예외를 기본 오류 스트림에 출력하는 메서드
        }

    }

}
