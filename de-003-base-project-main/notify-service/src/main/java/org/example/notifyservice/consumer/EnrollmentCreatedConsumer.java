package org.example.notifyservice.consumer;

import org.example.notifyservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EnrollmentCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentCreatedConsumer.class);

    private final EmailService emailService;

    public EnrollmentCreatedConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "enrollment-created")
    public void consume(String email) {
        if (!StringUtils.hasText(email)) {
            log.warn("Bỏ qua sự kiện enrollment có email rỗng");
            return;
        }
        log.info("Nhận sự kiện tạo enrollment thành công cho email: {}", email);
        emailService.sendEnrollmentCreatedEmail(email);
        log.info("Đã gửi email xác nhận đăng ký khóa học tới: {}", email);
    }
}
