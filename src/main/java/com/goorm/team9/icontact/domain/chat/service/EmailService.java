package com.goorm.team9.icontact.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendChatRequestNotification(String to, String senderNickname) {
        String subject = "[iContact] 새로운 채팅 요청이 도착했습니다!";
        String text = senderNickname + "님이 채팅을 요청했습니다. 확인해보세요!";
        sendSimpleMail(to, subject, text);
    }

    public void sendNewMessageNotification(String to, String senderNickname, String messagePreview) {
        String subject = "[iContact] 새 메시지가 도착했습니다!";
        String text = senderNickname + "님이 보낸 메시지:\n\n\"" + messagePreview + "\"\n\n채팅방에서 확인해보세요!";
        sendSimpleMail(to, subject, text);
    }
}
