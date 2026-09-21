package com.colmcooney.fruitmachine.controller;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "How many slots the machine has. Defaults to 4.", example = "4")
        @Min(MachineConfig.MIN_SLOTS) @Max(MachineConfig.MAX_SLOTS) Integer slotCount,

        @Schema(description = "The colours a slot can show, unique ignoring case. Defaults to black, white, green, yellow.",
                example = "[\"black\", \"white\", \"green\", \"yellow\"]")
        @Size(min = MachineConfig.MIN_COLOURS, max = MachineConfig.MAX_COLOURS) List<@NotBlank String> colours,

        @Schema(description = "How many adjacent slots of the same colour win the small prize. Between 2 and the slot count. "
                + "Defaults to 2.", example = "2")
        @Min(MachineConfig.MIN_ADJACENT_MATCH_LENGTH) Integer adjacentMatchLength,

        @Schema(description = "What one play costs, in minor units.", example = "100")
        @NotNull @Positive @Max(CreateMachineRequest.MAX_MONEY) Long playCost,

        @Schema(description = "The money the machine holds to begin with, in minor units.", example = "10000")
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
