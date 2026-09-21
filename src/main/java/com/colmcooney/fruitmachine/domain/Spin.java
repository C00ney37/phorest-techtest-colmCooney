package com.colmcooney.fruitmachine.domain;

import java.util.List;

/** The colours showing in each slot after a spin, in slot order. */
public record Spin(List<String> slots) {

    public Spin {
        slots = List.copyOf(slots);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("A spin must show at least one slot");
        }
    }

    /** True when every slot shows the same colour. */
    public boolean isJackpot() {
        String first = slots.getFirst();
        return slots.stream().allMatch(first::equals);
    }
}
