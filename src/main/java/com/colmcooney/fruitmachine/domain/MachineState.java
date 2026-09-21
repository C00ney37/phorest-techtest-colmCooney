package com.colmcooney.fruitmachine.domain;

/**
 * The part of a machine that changes as it is played: the money it holds and the free plays owed to the player.
 * Money is in minor units (e.g. cents).
 */
public record MachineState(long floatAmount, long freePlays) {

    public MachineState {
        if (floatAmount < 0) {
            throw new IllegalArgumentException("The float must not be negative, got " + floatAmount);
        }
        if (freePlays < 0) {
            throw new IllegalArgumentException("Free plays must not be negative, got " + freePlays);
        }
    }
}
