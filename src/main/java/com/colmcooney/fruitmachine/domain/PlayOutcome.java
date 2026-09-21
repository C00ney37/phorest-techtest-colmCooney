package com.colmcooney.fruitmachine.domain;

/**
 * The result of one play of the machine.
 *
 * @param spin              what the slots showed
 * @param prizeTier         the prize won, or {@link PrizeTier#NONE}
 * @param payout            money actually paid out of the float (less than the prize when the float was too small)
 * @param freePlaysCredited free plays awarded to cover the part of a prize the float could not pay
 * @param usedFreePlay      true when this play was free, so no stake was added to the float
 * @param stateAfter        the machine's float and free plays once the play is settled
 */
public record PlayOutcome(
        Spin spin,
        PrizeTier prizeTier,
        long payout,
        long freePlaysCredited,
        boolean usedFreePlay,
        MachineState stateAfter) {
}
