CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       keycloak_id     VARCHAR(255) NOT NULL UNIQUE,
                       email           VARCHAR(255) NOT NULL,
                       full_name       VARCHAR(255) NOT NULL,
                       phone           VARCHAR(50),
                       blocked         BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees (
                           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           keycloak_id     VARCHAR(255) UNIQUE,
                           full_name       VARCHAR(255) NOT NULL,
                           email           VARCHAR(255),
                           phone           VARCHAR(50),
                           active          BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE services (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name            VARCHAR(255) NOT NULL,
                          description     TEXT,
                          price_cents     INTEGER NOT NULL CHECK (price_cents >= 0),
                          duration_min    INTEGER NOT NULL CHECK (duration_min > 0),
                          duration_max    INTEGER NOT NULL CHECK (duration_max >= duration_min),
                          active          BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_options (
                                 id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 service_id          UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
                                 name                VARCHAR(255) NOT NULL,
                                 price_delta_cents    INTEGER NOT NULL,
                                 duration_delta_min   INTEGER NOT NULL,
                                 active              BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_services (
                                   employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
                                   service_id      UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
                                   created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (employee_id, service_id)
);

CREATE TABLE employee_availability (
                                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
                                       day_of_week     INTEGER NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
                                       start_time      TIME NOT NULL,
                                       end_time        TIME NOT NULL,
                                       is_lunch_break  BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_blocks (
                                 id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
                                 block_date      DATE NOT NULL,
                                 start_time      TIME NOT NULL,
                                 end_time        TIME NOT NULL,
                                 reason          VARCHAR(255),
                                 created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE holidays (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          holiday_date    DATE NOT NULL UNIQUE,
                          name            VARCHAR(255),
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE studio_settings (
                                 id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 slot_minutes    INTEGER NOT NULL DEFAULT 15,
                                 buffer_minutes  INTEGER NOT NULL DEFAULT 10,
                                 timezone        VARCHAR(50) NOT NULL DEFAULT 'America/Sao_Paulo',
                                 created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO studio_settings (slot_minutes, buffer_minutes, timezone) VALUES (15, 10, 'America/Sao_Paulo');

CREATE TABLE appointments (
                              id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              client_user_id          UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                              employee_id             UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
                              service_id              UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
                              appointment_date        DATE NOT NULL,
                              start_time              TIME NOT NULL,
                              end_time                TIME NOT NULL,
                              status                  VARCHAR(20) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED', 'NO_SHOW')),
                              price_cents             INTEGER NOT NULL,
                              duration_min            INTEGER NOT NULL,
                              client_name             VARCHAR(255) NOT NULL,
                              client_email            VARCHAR(255) NOT NULL,
                              client_phone            VARCHAR(50),
                              service_options_snapshot JSONB,
                              cancelled_at            TIMESTAMP,
                              cancel_reason           VARCHAR(255),
                              created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointment_substitutions (
                                           id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           appointment_id      UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
                                           previous_employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
                                           new_employee_id     UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
                                           substituted_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           substituted_by      VARCHAR(255)
);

CREATE TABLE expenses (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          category        VARCHAR(50) NOT NULL CHECK (category IN ('FIXED', 'VARIABLE', 'MATERIALS', 'EMPLOYEES', 'OTHER')),
                          amount_cents    INTEGER NOT NULL,
                          expense_date    DATE NOT NULL,
                          description     VARCHAR(500),
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_employee_date ON appointments(employee_id, appointment_date, start_time);
CREATE INDEX idx_appointments_client_date ON appointments(client_user_id, appointment_date);
CREATE INDEX idx_appointments_date_status ON appointments(appointment_date, status);
CREATE INDEX idx_employee_blocks_employee_date ON employee_blocks(employee_id, block_date);
CREATE INDEX idx_employee_availability_employee ON employee_availability(employee_id);
CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_users_keycloak ON users(keycloak_id);
CREATE INDEX idx_employees_keycloak ON employees(keycloak_id);
