package unla.tp.tp_distribuidos.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unla.tp.tp_distribuidos.TpDistribuidosApplication;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfiguration {

    private final TpDistribuidosApplication tpDistribuidosApplication;

    SwaggerConfiguration(TpDistribuidosApplication tpDistribuidosApplication) {
        this.tpDistribuidosApplication = tpDistribuidosApplication;
    }

    private SecurityScheme createBasicScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("basic");
    }

    @Bean
    OpenAPI customOpenAPI() {
        OpenAPI api = new OpenAPI();

        api.addSecurityItem(new SecurityRequirement().
                        addList("BasicAuth"))
                .components(new Components().addSecuritySchemes
                        ("BasicAuth", createBasicScheme()))
                .info(new Info()
                        .title("Rentar")
                        .version("1.0.0")
                        .description("API REST para gestionar el sistema de Rentar")
                        .license(new License().name("MIT License")));

        return api;
    }
}

