package com.colmcooney.fruitmachine.repository;

import com.colmcooney.fruitmachine.domain.Machine;
import java.util.Optional;

/** Where machines are kept between calls. */
public interface MachineRepository {

    /** Stores the machine, replacing any earlier version with the same id. */
    Machine save(Machine machine);

    Optional<Machine> findById(String id);
}
