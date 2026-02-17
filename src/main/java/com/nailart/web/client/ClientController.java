package com.nailart.web.client;

import com.nailart.application.scheduling.SchedulingService;
import com.nailart.application.users.UserService;
import com.nailart.infrastructure.persistence.entity.AppointmentEntity;
import com.nailart.infrastructure.persistence.entity.UserEntity;
import com.nailart.web.dto.AppointmentDto;
import com.nailart.web.dto.CreateAppointmentRequest;
import com.nailart.web.dto.UserDto;
import com.nailart.web.mapper.DtoMapper;
import com.nailart.web.support.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "2. Cliente", description = "Área do cliente. Exige role **client**. Perfil, criar/listar/cancelar agendamentos (cancelamento até 6h antes).")
public class ClientController {

    private final UserService userService;
    private final SchedulingService schedulingService;

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obter perfil", description = "Retorna o perfil do cliente autenticado. Se ainda não existir no sistema, cria a partir do JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil do cliente"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public UserDto getProfile() {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        UserEntity user = userService.findByKeycloakId(keycloakId)
                .orElseGet(() -> syncUserFromJwt(keycloakId));
        return DtoMapper.toUserDto(user);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome e/ou telefone do cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public UserDto updateProfile(@RequestBody UpdateProfileRequest request) {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        UserEntity user = userService.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalStateException("Perfil não encontrado."));
        UserEntity updated = userService.updateProfile(user.getId(), request.getFullName(), request.getPhone());
        return DtoMapper.toUserDto(updated);
    }

    @PostMapping(value = "/appointments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar agendamento",
            description = "Cria um agendamento para o cliente autenticado. Horário deve estar em GET /api/public/availability. " +
                    "Valida conflitos e disponibilidade antes de salvar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado (CONFIRMED)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "422", description = "Cliente bloqueado, horário indisponível ou conflito"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public AppointmentDto createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        UserEntity user = userService.findByKeycloakId(keycloakId)
                .orElseGet(() -> syncUserFromJwt(keycloakId));
        AppointmentEntity a = schedulingService.createAppointment(
                user.getId(),
                request.getEmployeeId(),
                request.getServiceId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getOptionIds()
        );
        return DtoMapper.toAppointmentDto(a);
    }

    @GetMapping(value = "/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar meus agendamentos", description = "Lista agendamentos do cliente no período (from/to).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<AppointmentDto> listMyAppointments(
            @Parameter(description = "Data inicial (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        UserEntity user = userService.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalStateException("Perfil não encontrado."));
        List<AppointmentEntity> list = schedulingService.getAppointmentsByClient(user.getId(), from, to);
        return list.stream().map(DtoMapper::toAppointmentDto).toList();
    }

    @PostMapping(value = "/appointments/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Cancelar agendamento",
            description = "Cancela um agendamento do cliente. Permitido apenas até **6 horas antes** do horário de início. " +
                    "Só cancela agendamentos próprios e com status CONFIRMED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Agendamento não pertence ao cliente"),
            @ApiResponse(responseCode = "422", description = "Fora do prazo ou já cancelado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public void cancelAppointment(
            @Parameter(description = "UUID do agendamento", required = true) @PathVariable UUID id) {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        UserEntity user = userService.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalStateException("Perfil não encontrado."));
        schedulingService.cancelByClient(id, user.getId());
    }

    private UserEntity syncUserFromJwt(String keycloakId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = keycloakId;
        String fullName = "Cliente";
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            email = jwt.getClaim("email") != null ? jwt.getClaim("email") : keycloakId;
            fullName = jwt.getClaim("name") != null ? jwt.getClaim("name") : (jwt.getClaim("preferred_username") != null ? jwt.getClaim("preferred_username") : "Cliente");
        }
        return userService.createOrUpdateFromKeycloak(keycloakId, email, fullName, null);
    }

    @lombok.Data
    @Schema(description = "Dados para atualizar perfil do cliente")
    public static class UpdateProfileRequest {
        @Schema(description = "Nome completo")
        private String fullName;
        @Schema(description = "Telefone")
        private String phone;
    }
}
