package com.colmcooney.fruitmachine.service.rule;

import java.util.List;

/** The rule sets the machine can be built with. */
public final class PrizeRules {

    private PrizeRules() {
    }

    /** The original game, highest priority first: jackpot, then full house (half the float), then small prize (5x cost). */
    public static List<PrizeRule> standard() {
        return List.of(new JackpotPrizeRule(), new FullHousePrizeRule(2), new SmallPrizeRule(5));
    }
}
