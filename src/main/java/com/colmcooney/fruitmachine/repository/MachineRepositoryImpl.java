package com.colmcooney.fruitmachine.repository;

import com.colmcooney.fruitmachine.domain.Machine;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps machines in memory, so they are lost when the service restarts. */
public class MachineRepositoryImpl implements MachineRepository {

    private final Map<String, Machine> machinesById = new ConcurrentHashMap<>();

    @Override
    public Machine save(Machine machine) {
        machinesById.put(machine.id(), machine);
        return machine;
    }

    @Override
    public Optional<Machine> findById(String id) {
        return Optional.ofNullable(machinesById.get(id));
    }
}
