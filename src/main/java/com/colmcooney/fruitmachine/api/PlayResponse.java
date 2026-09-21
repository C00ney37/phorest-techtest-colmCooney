package com.colmcooney.fruitmachine.api;

import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * The result of one play. {@code floatAmount} and {@code freePlays} are the machine's totals after the play.
 * Money is in minor units, e.g. cents.
 */
public record PlayResponse(
        @Schema(description = "The colour showing in each slot, in slot order.", example = "[\"green\", \"green\", \"white\", \"black\"]")
        List<String> slots,
        @Schema(description = "The prize won, or NONE.") PrizeTier prizeTier,
        @Schema(description = "Money actually paid out of the float. Less than the prize when the float could not cover it.",
                example = "500")
        long payout,
        @Schema(description = "Free plays awarded for the part of a prize the float could not pay.", example = "0")
        long freePlaysCredited,
        @Schema(description = "True when this play was free, so no stake was taken.", example = "false")
        boolean usedFreePlay,
        @Schema(description = "The machine's float after this play.", example = "9600") long floatAmount,
        @Schema(description = "Free plays owed to the player after this play.", example = "0") long freePlays) {

    static PlayResponse from(PlayOutcome outcome) {
        return new PlayResponse(
                outcome.spin().slots(),
                outcome.prizeTier(),
                outcome.payout(),
                outcome.freePlaysCredited(),
                outcome.usedFreePlay(),
                outcome.stateAfter().floatAmount(),
                outcome.stateAfter().freePlays());
    }
}
