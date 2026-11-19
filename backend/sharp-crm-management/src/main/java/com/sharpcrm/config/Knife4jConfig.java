package com.sharpcrm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 接口文档配置
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    private static final String TOKEN_SCHEME = "token";

    @Bean
    public OpenAPI qkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("锐客 CRM 招生管理系统 API")
                        .description("AI 职业教育培训机构客户关系管理系统接口文档")
                        .version("1.0")
                        .contact(new Contact().name("sharp-crm-team")))
                .components(new Components().addSecuritySchemes(TOKEN_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("token")
                                .description("登录后返回的 JWT，放在请求头 token 字段")))
                .addSecurityItem(new SecurityRequirement().addList(TOKEN_SCHEME));
    }
}
