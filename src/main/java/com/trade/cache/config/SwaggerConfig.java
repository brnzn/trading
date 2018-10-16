package com.trade.cache.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

import static java.util.Arrays.asList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.trade.cache.web"))
                .build()
                .apiInfo(apiInfo()).globalOperationParameters(
                        asList(new ParameterBuilder()
                                .name("vendor")
                                .description("Vendor Name")
                                .modelRef(new ModelRef("string"))
                                .parameterType("query")
                                .required(false)
                                .build(),

                                (new ParameterBuilder()
                                        .name("symbol")
                                        .description("Traded Instrument Symbol")
                                        .modelRef(new ModelRef("string"))
                                        .parameterType("query")
                                        .required(false)
                                        .build())));
    }

    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .defaultModelRendering(ModelRendering.MODEL)
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("Traded Instruments Cache", "Receive traded instruments from vendors, " +
                "and provide price information to clients. " +
                "To use the API you must provide vendor or symbol (but not both)"
                , ""
                , "", null, "", "", Collections.emptyList());
    }
}