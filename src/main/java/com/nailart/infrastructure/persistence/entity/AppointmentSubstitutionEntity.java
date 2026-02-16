package com.nailart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_substitutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSubstitutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_employee_id", nullable = false)
    private EmployeeEntity previousEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_employee_id", nullable = false)
    private EmployeeEntity newEmployee;

    @Column(name = "substituted_at", nullable = false)
    private Instant substitutedAt;

    @Column(name = "substituted_by", length = 255)
    private String substitutedBy;

    @PrePersist
    void prePersist() {
        if (substitutedAt == null) substitutedAt = Instant.now();
    }
}
