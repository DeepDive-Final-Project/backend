package com.goorm.team9.icontact.chat.service;

import com.goorm.team9.icontact.domain.chat.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Test
    @DisplayName("채팅 요청 알림 메일이 정상적으로 전송된다.")
    void sendChatRequestNotification_success() {
        // Given
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(javaMailSender);

        String receiverEmail = "test@naver.com";
        String senderNickname = "Noah1";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendChatRequestNotification(receiverEmail, senderNickname);

        // Then
        verify(javaMailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertThat(sentMessage.getTo()).containsExactly(receiverEmail);
        assertThat(sentMessage.getSubject()).contains("채팅 요청");
        assertThat(sentMessage.getText()).contains(senderNickname);
    }

    @Test
    @DisplayName("새 메시지 알림 메일이 정상적으로 전송된다.")
    void sendNewMessageNotification_success() {
        // Given
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(javaMailSender);

        String receiverEmail = "test@naver.com";
        String senderNickname = "Noah1";
        String content = "테스트 메시지입니다.";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendNewMessageNotification(receiverEmail, senderNickname, content);

        // Then
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage mail = captor.getValue();
        assertThat(mail.getTo()).containsExactly(receiverEmail);
        assertThat(mail.getSubject()).contains("새 메시지");
        assertThat(mail.getText()).contains(content).contains(senderNickname);
    }
}
