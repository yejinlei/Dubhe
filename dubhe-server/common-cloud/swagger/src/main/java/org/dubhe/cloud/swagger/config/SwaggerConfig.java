/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.cloud.swagger.config;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.constant.AuthConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

/**
 * @description  api?????? /swagger-ui.html
 * @date 2020-03-25
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * ????????????????????????????????????????????????????????????
     */
    @Value("${swagger.enabled:true}")
    private Boolean enabled;

    @Value("${spring.application.name}")
    private String applicationName;


    @Bean
    @SuppressWarnings("all")
    public Docket createRestApi() {
        List<Parameter> pars = new ArrayList<>();
        pars.add(new ParameterBuilder()
                .name(AuthConst.AUTHORIZATION)
                .description("??????")
                .modelRef(
                        new ModelRef("string")
                )
                .parameterType("header")
                .required(false)
                .build()
        );
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(enabled)
                .apiInfo(apiInfo())
                .select()
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build().directModelSubstitute(Timestamp.class, Date.class)
                .globalOperationParameters(pars);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(applicationName)
                .version("1.0")
                .build();
    }

}

/**
 * ???Pageable???????????????swagger???
 */
@Configuration
class SwaggerDataConfig {

    @Bean
    public AlternateTypeRuleConvention pageableConvention(final TypeResolver resolver) {
        return new AlternateTypeRuleConvention() {
            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }

            @Override
            public List<AlternateTypeRule> rules() {
                return newArrayList(newRule(resolver.resolve(Pageable.class), resolver.resolve(Page.class)));
            }
        };
    }

    @ApiModel
    @Data
    private static class Page {
        @ApiModelProperty("?????? (0..N)")
        private Integer page;

        @ApiModelProperty("?????????????????????")
        private Integer size;

        @ApiModelProperty("??????????????????????????????property[,asc | desc]??? ?????????????????????????????? ?????????????????????????????????id,asc")
        private List<String> sort;
    }
}
