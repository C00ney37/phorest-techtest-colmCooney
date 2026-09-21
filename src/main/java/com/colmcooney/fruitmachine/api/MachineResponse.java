package com.colmcooney.fruitmachine.api;

import com.colmcooney.fruitmachine.domain.Machine;
import java.util.List;

/** A machine's configuration and current state. Money is in minor units, e.g. cents. */
public record MachineResponse(
        String id,
        int slotCount,
        List<String> colours,
        int adjacentMatchLength,
        long playCost,
        long floatAmount,
        long freePlays) {

    static MachineResponse from(Machine machine) {
        return new MachineResponse(
                machine.id(),
                machine.config().slotCount(),
                machine.config().colours(),
                machine.config().adjacentMatchLength(),
                machine.config().playCost(),
                machine.state().floatAmount(),
                machine.state().freePlays());
    }
}
