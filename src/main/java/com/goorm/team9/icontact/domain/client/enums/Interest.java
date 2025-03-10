package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Interest implements EnumWithDescription {

    Android_Development("Android 개발"),
    iOS_Development("iOS 개발"),
    Cross_Platform_Development("크로스플랫폼 개발"),
    Data_Engineering("데이터 엔지니어링"),
    Data_Analysis("데이터 분석"),
    Machine_Learning_And_Deep_Learning("머신/딥러닝"),
    Big_Data_Processing("빅데이터 처리"),
    Cloud_Services("A클라우드 서비스"),
    Container_Technologies("컨테이너 기술"),
    CI_And_CD("CI/CD"),
    Serverless_Architecture("서버리스 아키텍처"),
    Natural_Language_Processing("자연어 처리"),
    Computer_Vision("컴퓨터 비전"),
    Recommendation_System("추천시스템"),
    Reinforcement_Learning("강화학습"),
    WebSecurity("웹 보안"),
    Network_Security("네트워크 보안"),
    Blockchain("블록체인"),
    Smart_Contract("스마트 컨트랙트"),
    Infiltration_testing("침투 테스트"),
    and_hacking_response("해킹 대응"),
    Game_Engine("게임 엔진"),
    Game_Server_Development("게임 서버 개발"),
    AR_Or_VR_Development("AR/VR 개발"),
    Embedded_System_Development("임베디드 시스템 개발"),
    Smart_Device_Development("스마트 디바이스 개발"),
    Robot_Engineering("로봇 공학"),
    Linux_Kernel_Development("리눅스 커널 개발"),
    System_Programming("시스템 프로그래밍"),
    Distributed_Systems("분산 시스템"),
    SQL_And_NoSQL_Database("SQL & NoSQL"),
    Search_Engine("검색 엔진"),
    Database_Optimization("데이터베이스 최적화"),
    Microservice_Architecture("마이크로서비스 아키텍처"),
    Event_Driven_Architecture("이벤트 드리븐 아키텍처"),
    TDD_BDD_DDD("TDD / BDD / DDD"),
    Agile_Development("애자일 개발"),
    Open_Source_Contributions("오픈소스 기여"),
    Create_Technical_Blog("기술 블로그 작성"),
    Development_Community_Activities("개발 커뮤니티 활동"),
    Start_ups("스타트업 & 창업");

    private final String description;

    Interest(String description) {
        this.description = description;
    }
}
