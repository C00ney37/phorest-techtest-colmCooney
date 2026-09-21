package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.PlayOutcome;

/**
 * Looks after machines between calls: sets them up, finds them and keeps their state up to date.
 * The rules of a single play live in {@link FruitMachineService}; this service applies them to a stored machine.
 */
public interface MachineService {

    /** Sets up a new machine holding {@code startingFloat} and returns it with its generated id. */
    Machine createMachine(MachineConfig config, long startingFloat);

    /** @throws MachineNotFoundException if there is no machine with this id */
    Machine getMachine(String machineId);

    /**
     * Plays the machine once and stores its new float and free plays. Plays on the same machine are applied one
     * at a time, so concurrent callers never overwrite each other's changes.
     *
     * @throws MachineNotFoundException if there is no machine with this id
     */
    PlayOutcome play(String machineId);
}
