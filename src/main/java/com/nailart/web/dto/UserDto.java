package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Usuário (cliente) do sistema")
public class UserDto {
    @Schema(description = "Identificador único")
    private UUID id;
    @Schema(description = "E-mail")
    private String email;
    @Schema(description = "Nome completo")
    private String fullName;
    @Schema(description = "Telefone")
    private String phone;
    @Schema(description = "Se o cliente está bloqueado pelo admin", example = "false")
    private Boolean blocked;
}
