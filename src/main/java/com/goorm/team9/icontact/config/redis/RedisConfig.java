package com.goorm.team9.icontact.config.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public CommandLineRunner testRedisConnection() {
        return args -> {
            try {
                RedisConnection connection = redisConnectionFactory.getConnection();
                String pingResponse = connection.ping();
                System.out.println("✅ Redis 서버 연결 성공: " + pingResponse);
            } catch (Exception e) {
                System.err.println("❌ Redis 연결 실패: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
