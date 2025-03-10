package com.goorm.team9.icontact.domain.client.enums;

import com.goorm.team9.icontact.domain.common.EnumWithDescription;
import lombok.Getter;

@Getter
public enum Framework implements EnumWithDescription {

    SPRING("스프링"),
    SPRINGBOOT("스프링 부트"),
    Django("Django"),
    FLASK("FLASK"),
    FastAPI("FastAPI"),
    DOTNET("닷넷"),
    ASP_DOTNET_CORE("ASP.NET CORE"),
    Qt("Qt"),
    Boost("Boost"),
    Jin("Jin"),
    Echo("Echo"),
    Ruby_on_Rails("루비 온 레일즈"),
    Sinatra("Sinatra"),
    Express_js("Express.js"),
    NestJS("NestJS"),
    Ktor("Ktor"),
    Jetpack_Compose("젝팩 컴포즈"),
    Laravel("Laravel"),
    Symfony("Symfony"),
    Rocket("Rocket"),
    Actix("Actix"),
    React("React"),
    Vue_js("Vue.js"),
    Angular("Angular"),
    Next_js("Next.js"),
    Nuxt_js("Nuxt.js");

    private final String description;

    Framework(String description) {
        this.description = description;
    }
}
