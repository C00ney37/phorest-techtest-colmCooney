package com.colmcooney.fruitmachine.support;

import java.util.List;
import java.util.stream.IntStream;

/** Test helper for building large colour lists. */
public final class Colours {

    private Colours() {
    }

    /** {@code count} distinct colours named colour-0, colour-1, ... */
    public static List<String> numbered(int count) {
        return IntStream.range(0, count).mapToObj(colourNumber -> "colour-" + colourNumber).toList();
    }
}
