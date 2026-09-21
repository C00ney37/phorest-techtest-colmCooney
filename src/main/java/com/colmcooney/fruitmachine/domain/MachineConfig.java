package com.colmcooney.fruitmachine.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The fixed rules of a machine.
 * Colours are compared case-insensitively, so "Black" and "black" count as duplicates.
 * The play cost is in minor units (e.g. cents).
 *
 * @param slotCount           how many slots the machine has
 * @param colours             the colours each slot can show
 * @param adjacentMatchLength how many adjacent slots must show the same colour to win the small prize (2 in the original game)
 * @param playCost            what one play costs
 */
public record MachineConfig(int slotCount, List<String> colours, int adjacentMatchLength, long playCost) {

    public static final int MIN_SLOTS = 2;
    public static final int MIN_COLOURS = 2;
    public static final int MIN_ADJACENT_MATCH_LENGTH = 2;

    /** Upper limits that keep a single play, and the response describing it, a sensible size. */
    public static final int MAX_SLOTS = 100_000;
    public static final int MAX_COLOURS = 10_000;

    /** The original game: four slots, each showing black, white, green or yellow, with a pair of adjacent slots winning. */
    public static final int DEFAULT_SLOT_COUNT = 4;
    public static final List<String> DEFAULT_COLOURS = List.of("black", "white", "green", "yellow");
    public static final int DEFAULT_ADJACENT_MATCH_LENGTH = 2;

    public MachineConfig {
        if (slotCount < MIN_SLOTS || slotCount > MAX_SLOTS) {
            throw new InvalidMachineConfigException(
                    "A machine needs between " + MIN_SLOTS + " and " + MAX_SLOTS + " slots, got " + slotCount);
        }
        colours = List.copyOf(colours);
        if (colours.size() < MIN_COLOURS || colours.size() > MAX_COLOURS) {
            throw new InvalidMachineConfigException(
                    "A machine needs between " + MIN_COLOURS + " and " + MAX_COLOURS + " colours, got " + colours.size());
        }
        requireDistinctNonBlank(colours);
        if (adjacentMatchLength < MIN_ADJACENT_MATCH_LENGTH || adjacentMatchLength > slotCount) {
            throw new InvalidMachineConfigException("The adjacent match length must be between " + MIN_ADJACENT_MATCH_LENGTH
                    + " and the slot count (" + slotCount + "), got " + adjacentMatchLength);
        }
        if (playCost <= 0) {
            throw new InvalidMachineConfigException("The play cost must be positive, got " + playCost);
        }
    }

    public static MachineConfig classic(long playCost) {
        return new MachineConfig(DEFAULT_SLOT_COUNT, DEFAULT_COLOURS, DEFAULT_ADJACENT_MATCH_LENGTH, playCost);
    }

    private static void requireDistinctNonBlank(List<String> colours) {
        Set<String> seen = new HashSet<>();
        for (String colour : colours) {
            if (colour.isBlank()) {
                throw new InvalidMachineConfigException("Colour names must not be blank");
            }
            if (!seen.add(colour.toLowerCase(Locale.ROOT))) {
                throw new InvalidMachineConfigException("Duplicate colour: " + colour);
            }
        }
    }
}
