package com.colmcooney.fruitmachine.controller;

import com.colmcooney.fruitmachine.domain.Machine;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** A machine's configuration and current state. Money is in minor units, e.g. cents. */
public record MachineResponse(
        @Schema(description = "The machine's id, used in the other endpoints.", example = "52009c9d-77da-4b2c-929b-d9b46d1287ff")
        String id,
        @Schema(description = "How many slots the machine has.", example = "4") int slotCount,
        @Schema(description = "The colours a slot can show.") List<String> colours,
        @Schema(description = "How many adjacent slots of the same colour win the small prize.", example = "2")
        int adjacentMatchLength,
        @Schema(description = "What one play costs.", example = "100") long playCost,
        @Schema(description = "The money the machine currently holds.", example = "10000") long floatAmount,
        @Schema(description = "Free plays currently owed to the player.", example = "0") long freePlays) {

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
