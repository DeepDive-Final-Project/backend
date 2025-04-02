package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Interest implements EnumWithDescription {

    // 기술 & 개발
    AI_Machine_Learning("AI/머신러닝", "DEV"),
    Server_Development("서버 개발", "DEV"),
    API_Development("API 개발", "DEV"),
    Microservice("마이크로서비스", "DEV"),
    Web_Performance_Optimization("웹 성능 최적화", "DEV"),
    Edge_Computing("엣지 컴퓨팅", "DEV"),
    iOS_Development("iOS 개발", "DEV"),
    Android_Development("Android 개발", "DEV"),
    Cross_Platform_Development("크로스플랫폼 개발", "DEV"),
    DevOps_Infrastructure("DevOps/인프라", "DEV"),
    Security_Hacking("보안/해킹", "DEV"),
    Game_Development("게임 개발", "DEV"),

    // 프로덕트/비즈니스
    Product_Strategy_Roadmap("프로덕트 전략/로드맵", "PD"),
    User_Research("사용자 리서치", "PD"),
    Growth_Hacking_Marketing("그로스 해킹/마케팅", "PD"),
    Data_Analysis("데이터 분석", "PD"),
    B2B("B2B", "PD"),
    B2C("B2C", "PD"),

    // 디자인/사용자 경험
    UX_Planning_Strategy("UX 기획/전략", "DS"),
    UI_Design("UI 디자인", "DS"),
    Accessibility("접근성", "DS"),
    Design_System("디자인 시스템", "DS"),
    Prototyping_Interaction_Design("프로토타이핑 / 인터랙션 디자인", "DS"),
    ThreeD_Motion_Design("3D & 모션 디자인", "DS");

    private final String description;
    private final String apiCode;

    Interest(String description, String apiCode) {
        this.description = description;
        this.apiCode = apiCode;
    }

}
