package com.colmcooney.fruitmachine.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The fixed rules of a machine: how many slots it has, which colours each slot can show, and what a play costs.
 * Colours are compared case-insensitively, so "Black" and "black" count as duplicates.
 * The play cost is in minor units (e.g. cents).
 */
public record MachineConfig(int slotCount, List<String> colours, long playCost) {

    public static final int MIN_SLOTS = 2;
    public static final int MIN_COLOURS = 2;

    public MachineConfig {
        if (slotCount < MIN_SLOTS) {
            throw new IllegalArgumentException("A machine needs at least " + MIN_SLOTS + " slots, got " + slotCount);
        }
        colours = List.copyOf(colours);
        if (colours.size() < MIN_COLOURS) {
            throw new IllegalArgumentException("A machine needs at least " + MIN_COLOURS + " colours, got " + colours.size());
        }
        requireDistinctNonBlank(colours);
        if (playCost <= 0) {
            throw new IllegalArgumentException("The play cost must be positive, got " + playCost);
        }
    }

    /** The original game: four slots, each showing black, white, green or yellow. */
    public static MachineConfig classic(long playCost) {
        return new MachineConfig(4, List.of("black", "white", "green", "yellow"), playCost);
    }

    private static void requireDistinctNonBlank(List<String> colours) {
        Set<String> seen = new HashSet<>();
        for (String colour : colours) {
            if (colour.isBlank()) {
                throw new IllegalArgumentException("Colour names must not be blank");
            }
            if (!seen.add(colour.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate colour: " + colour);
            }
        }
    }
}
