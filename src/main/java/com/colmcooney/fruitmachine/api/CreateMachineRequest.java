package com.colmcooney.fruitmachine.api;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Describes a machine to set up. Slot count, colours and adjacent match length are optional and default to the
 * original game (4 slots, black/white/green/yellow, a pair wins). Money is in minor units, e.g. cents.
 */
public record CreateMachineRequest(
        @Min(MachineConfig.MIN_SLOTS) @Max(MachineConfig.MAX_SLOTS) Integer slotCount,
        @Size(min = MachineConfig.MIN_COLOURS, max = MachineConfig.MAX_COLOURS) List<@NotBlank String> colours,
        @Min(MachineConfig.MIN_ADJACENT_MATCH_LENGTH) Integer adjacentMatchLength,
        @NotNull @Positive @Max(CreateMachineRequest.MAX_MONEY) Long playCost,
        @NotNull @PositiveOrZero @Max(CreateMachineRequest.MAX_MONEY) Long startingFloat) {

    /** Keeps amounts far from overflowing a long as the float grows. */
    public static final long MAX_MONEY = 1_000_000_000_000L;

    /**
     * Fills in defaults for the optional fields. Rules that involve several fields, such as the adjacent match
     * length not exceeding the slot count, are checked by {@link MachineConfig} itself.
     */
    MachineConfig toConfig() {
        return new MachineConfig(
                slotCount != null ? slotCount : MachineConfig.DEFAULT_SLOT_COUNT,
                colours != null ? colours : MachineConfig.DEFAULT_COLOURS,
                adjacentMatchLength != null ? adjacentMatchLength : MachineConfig.DEFAULT_ADJACENT_MATCH_LENGTH,
                playCost);
    }
}
