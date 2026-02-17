package com.nailart.web.admin;

import com.nailart.application.users.UserService;
import com.nailart.infrastructure.persistence.entity.UserEntity;
import com.nailart.web.dto.UserDto;
import com.nailart.web.mapper.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "8. Admin - Usuários", description = "Listar clientes, ver perfil e bloquear/desbloquear. Exige role **admin**.")
public class AdminUsersController {

    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar todos os clientes")
    public List<UserDto> listAll() {
        return userService.listAll().stream().map(DtoMapper::toUserDto).toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Buscar cliente por ID")
    public UserDto getById(@PathVariable UUID id) {
        return DtoMapper.toUserDto(userService.getById(id));
    }

    @PatchMapping(value = "/{id}/block", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bloquear ou desbloquear cliente")
    public UserDto setBlocked(@PathVariable UUID id, @RequestBody BlockRequest request) {
        UserEntity u = userService.setBlocked(id, request.isBlocked());
        return DtoMapper.toUserDto(u);
    }

    @lombok.Data
    public static class BlockRequest {
        private boolean blocked;
    }
}
