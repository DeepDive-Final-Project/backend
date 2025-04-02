package com.goorm.team9.icontact.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Team-9 / 👀 I-Contact")
                .version("1.0.0")
                .description(
                        """
                        ## 🚀 I-Contact API 명세서
                        ### 🎨 디자이너
                        이준, 황소희
                        \n\n
                        ### 🖥️ 프론트엔드
                        안주현, 윤가은, 유지수
                        \n\n
                        ### 🌐 백엔드
                        이지은, 이서원, 성현아, 이정훈
                        \n\n
                        ### 🔗 링크
                        📌 [I-Contact-Web](https://www.i-contacts.link)
                        \n\n
                        📌 [GitHub](https://github.com/DeepDive-Final-Project)
                        \n\n
                        📌 [Notion](https://www.notion.so/I-Contact-Team-Project-1a47fd02709c8034a6f0f43be421f718)
                        \n\n
                        📌 [Jira](https://qweqwerty12321-1740141206278.atlassian.net/jira/software/projects/ICT/pages)
                        """
                );
    }

}
