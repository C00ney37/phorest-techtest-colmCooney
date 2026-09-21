package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;

public interface FruitMachineService {

    /**
     * Plays the machine once. Does not change {@code state}: the machine's new float and free plays
     * come back in {@link PlayOutcome#stateAfter()} for the caller to keep.
     */
    PlayOutcome play(MachineConfig config, MachineState state);
}
