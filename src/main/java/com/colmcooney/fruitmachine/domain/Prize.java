package com.colmcooney.fruitmachine.domain;

/** What a spin is worth: the tier it won and the amount owed to the player, before checking the float can cover it. */
public record Prize(PrizeTier tier, long amount) {

    public Prize {
        if (amount < 0) {
            throw new IllegalArgumentException("A prize amount must not be negative, got " + amount);
        }
    }

    public static Prize none() {
        return new Prize(PrizeTier.NONE, 0);
    }
}
