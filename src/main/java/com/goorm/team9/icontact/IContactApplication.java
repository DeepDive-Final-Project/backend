package com.goorm.team9.icontact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.goorm.team9.icontact")
@EnableJpaAuditing
public class IContactApplication {

    public static void main(String[] args) {
        SpringApplication.run(IContactApplication.class, args);
    }

}
