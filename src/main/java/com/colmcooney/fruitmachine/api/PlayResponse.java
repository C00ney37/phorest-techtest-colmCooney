package com.colmcooney.fruitmachine.api;

import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import java.util.List;

/**
 * The result of one play. {@code floatAmount} and {@code freePlays} are the machine's totals after the play.
 * Money is in minor units, e.g. cents.
 */
public record PlayResponse(
        List<String> slots,
        PrizeTier prizeTier,
        long payout,
        long freePlaysCredited,
        boolean usedFreePlay,
        long floatAmount,
        long freePlays) {

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
