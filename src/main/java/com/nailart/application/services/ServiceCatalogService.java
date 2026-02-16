package com.nailart.application.services;

import com.nailart.infrastructure.persistence.entity.ServiceEntity;
import com.nailart.infrastructure.persistence.entity.ServiceOptionEntity;
import com.nailart.infrastructure.persistence.repository.ServiceJpaRepository;
import com.nailart.infrastructure.persistence.repository.ServiceOptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceJpaRepository serviceRepo;
    private final ServiceOptionJpaRepository serviceOptionRepo;

    @Transactional(readOnly = true)
    public List<ServiceEntity> listActive() {
        return serviceRepo.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> listAll() {
        return serviceRepo.findAll();
    }

    @Transactional(readOnly = true)
    public ServiceEntity getById(UUID id) {
        ServiceEntity s = serviceRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        s.getOptions().size(); // force load options
        return s;
    }

    @Transactional
    public ServiceEntity create(String name, String description, int priceCents, int durationMin, int durationMax) {
        ServiceEntity s = ServiceEntity.builder()
                .name(name)
                .description(description)
                .priceCents(priceCents)
                .durationMin(durationMin)
                .durationMax(durationMax)
                .active(true)
                .build();
        return serviceRepo.save(s);
    }

    @Transactional
    public ServiceEntity update(UUID id, String name, String description, Integer priceCents,
                                Integer durationMin, Integer durationMax, Boolean active) {
        ServiceEntity s = getById(id);
        if (name != null) s.setName(name);
        if (description != null) s.setDescription(description);
        if (priceCents != null) s.setPriceCents(priceCents);
        if (durationMin != null) s.setDurationMin(durationMin);
        if (durationMax != null) s.setDurationMax(durationMax);
        if (active != null) s.setActive(active);
        return serviceRepo.save(s);
    }

    @Transactional
    public void delete(UUID id) {
        serviceRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ServiceOptionEntity> getOptions(UUID serviceId) {
        return serviceOptionRepo.findByServiceIdAndActiveTrue(serviceId);
    }

    @Transactional
    public ServiceOptionEntity addOption(UUID serviceId, String name, int priceDeltaCents, int durationDeltaMin) {
        ServiceEntity service = getById(serviceId);
        ServiceOptionEntity o = ServiceOptionEntity.builder()
                .service(service)
                .name(name)
                .priceDeltaCents(priceDeltaCents)
                .durationDeltaMin(durationDeltaMin)
                .active(true)
                .build();
        return serviceOptionRepo.save(o);
    }

    @Transactional
    public ServiceOptionEntity updateOption(UUID optionId, String name, Integer priceDeltaCents,
                                            Integer durationDeltaMin, Boolean active) {
        ServiceOptionEntity o = serviceOptionRepo.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("Opção não encontrada"));
        if (name != null) o.setName(name);
        if (priceDeltaCents != null) o.setPriceDeltaCents(priceDeltaCents);
        if (durationDeltaMin != null) o.setDurationDeltaMin(durationDeltaMin);
        if (active != null) o.setActive(active);
        return serviceOptionRepo.save(o);
    }
}
