package com.colmcooney.fruitmachine.domain;

/** The result of one play of the machine. */
public record PlayOutcome(Spin spin, boolean jackpot) {
}
