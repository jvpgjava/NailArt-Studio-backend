package com.nailart.web.admin;

import com.nailart.application.services.ServiceCatalogService;
import com.nailart.infrastructure.persistence.entity.ServiceEntity;
import com.nailart.infrastructure.persistence.entity.ServiceOptionEntity;
import com.nailart.web.dto.ServiceDto;
import com.nailart.web.dto.ServiceOptionDto;
import com.nailart.web.mapper.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
@Tag(name = "4. Admin - Serviços", description = "CRUD de serviços e opções. Exige **manager** ou **admin**.")
public class AdminServicesController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar todos os serviços")
    public List<ServiceDto> listAll() {
        return serviceCatalogService.listAll().stream().map(DtoMapper::toServiceDto).toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Buscar serviço por ID")
    public ServiceDto getById(@PathVariable UUID id) {
        ServiceEntity s = serviceCatalogService.getById(id);
        return DtoMapper.toServiceDto(s);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar serviço")
    public ServiceDto create(@Valid @RequestBody CreateServiceRequest request) {
        ServiceEntity s = serviceCatalogService.create(
                request.getName(),
                request.getDescription(),
                request.getPriceCents(),
                request.getDurationMin(),
                request.getDurationMax()
        );
        return DtoMapper.toServiceDto(s);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar serviço")
    public ServiceDto update(@PathVariable UUID id, @RequestBody UpdateServiceRequest request) {
        ServiceEntity s = serviceCatalogService.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getPriceCents(),
                request.getDurationMin(),
                request.getDurationMax(),
                request.getActive()
        );
        return DtoMapper.toServiceDto(s);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir serviço")
    public void delete(@PathVariable UUID id) {
        serviceCatalogService.delete(id);
    }

    @GetMapping(value = "/{id}/options", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar opções do serviço")
    public List<ServiceOptionDto> listOptions(@PathVariable UUID id) {
        return serviceCatalogService.getOptions(id).stream().map(DtoMapper::toServiceOptionDto).toList();
    }

    @PostMapping(value = "/{id}/options", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar opção ao serviço")
    public ServiceOptionDto addOption(@PathVariable UUID id, @Valid @RequestBody AddOptionRequest request) {
        ServiceOptionEntity o = serviceCatalogService.addOption(
                id,
                request.getName(),
                request.getPriceDeltaCents(),
                request.getDurationDeltaMin()
        );
        return DtoMapper.toServiceOptionDto(o);
    }

    @PutMapping(value = "/{id}/options/{optionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar opção")
    public ServiceOptionDto updateOption(@PathVariable UUID id, @PathVariable UUID optionId,
                                         @RequestBody UpdateOptionRequest request) {
        ServiceOptionEntity o = serviceCatalogService.updateOption(
                optionId,
                request.getName(),
                request.getPriceDeltaCents(),
                request.getDurationDeltaMin(),
                request.getActive()
        );
        return DtoMapper.toServiceOptionDto(o);
    }

    @lombok.Data
    public static class CreateServiceRequest {
        @NotBlank
        private String name;
        private String description;
        @Min(0)
        private int priceCents;
        @Min(1)
        private int durationMin;
        @Min(1)
        private int durationMax;
    }

    @lombok.Data
    public static class UpdateServiceRequest {
        private String name;
        private String description;
        private Integer priceCents;
        private Integer durationMin;
        private Integer durationMax;
        private Boolean active;
    }

    @lombok.Data
    public static class AddOptionRequest {
        @NotBlank
        private String name;
        private int priceDeltaCents;
        private int durationDeltaMin;
    }

    @lombok.Data
    public static class UpdateOptionRequest {
        private String name;
        private Integer priceDeltaCents;
        private Integer durationDeltaMin;
        private Boolean active;
    }
}
