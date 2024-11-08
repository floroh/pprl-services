package de.unileipzig.dbs.pprl.service.dataowner.controller.docs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("de.unileipzig.dbs.pprl.service.dataowner.controller")
public class SwaggerConfig {

  @Bean
  public OpenAPI springShopOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("PPRL Data Owner Service API")
        .description("Services for the data owners for privacy-preserving record linkage, " +
        "including analysing and encoding of the local dataset")
        .version("1.0")
        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
      );
  }

}
