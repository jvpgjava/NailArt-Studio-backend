package com.nailart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:7550").description("Ambiente local"),
                        new Server().url("/").description("Relativo ao host atual")
                ))
                .components(components())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private Info apiInfo() {
        return new Info()
                .title("NailArt Studio API")
                .description("""
                API REST do sistema **NailArt Studio** para estúdios de Nail Design.
                
                ## Autenticação
                - Endpoints sob `/api/public/**` são **públicos** (sem token).
                - Demais endpoints exigem **JWT** (Bearer) emitido pelo Keycloak.
                
                ## Perfis (roles)
                - **client** — Cliente: perfil, agendamentos, cancelamento até 6h antes
                - **employee** — Funcionário: ver própria agenda
                - **manager** — Gerente: agenda, funcionários, serviços, financeiro, substituição
                - **admin** — Administrador: acesso total + gestão de clientes (bloquear)
                
                ## Convenções
                - Valores monetários em **centavos** (price_cents, amount_cents).
                - Status de agendamento: `CONFIRMED`, `CANCELLED`, `NO_SHOW`.
                - Timezone: America/Sao_Paulo.
                """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("NailArt Studio")
                        .email("contato@nailartstudio.com"))
                .license(new License().name("Proprietário").url(""));
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT do Keycloak (realm nailart). Roles: admin, manager, employee, client."));
    }
}
