package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.PlayOutcome;

public interface FruitMachineService {

    /** Spins the machine described by {@code config} and reports what came up. */
    PlayOutcome play(MachineConfig config);
}
