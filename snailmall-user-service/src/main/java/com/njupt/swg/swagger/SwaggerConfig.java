package com.njupt.swg.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @Author swg.
 * @Date 2019/1/8 11:39
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Configuration
public class SwaggerConfig {
    // 接口版本号
    private final String version = "3.0";
    // 接口大标题
    private final String title = "快乐蜗牛商城V3.0文档";
    // 具体的描述
    private final String description = "用户服务";
    // 服务说明url
    private final String termsOfServiceUrl = "http://www.kingeid.com";
    // 接口作者联系方式
    private final Contact contact = new Contact("fourColor", "https://github.com/sunweiguo", "sunweiguode@gmail.com");

    @Bean
    public Docket buildDocket() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(buildApiInf())
                .select().build();
    }

    private ApiInfo buildApiInf() {
        return new ApiInfoBuilder().title(title).termsOfServiceUrl(termsOfServiceUrl).description(description)
                .version(version).contact(contact).build();

    }

}