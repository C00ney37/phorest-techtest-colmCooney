package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Spin;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

/** Picks each slot's colour uniformly and independently at random. */
public class SpinnerImpl implements Spinner {

    private final RandomGenerator random;

    public SpinnerImpl(RandomGenerator random) {
        this.random = random;
    }

    /** A spinner backed by {@link SecureRandom}, so outcomes can't be predicted from earlier spins. */
    public static SpinnerImpl secure() {
        return new SpinnerImpl(new SecureRandom());
    }

    @Override
    public Spin spin(MachineConfig config) {
        List<String> colours = config.colours();
        String[] slots = new String[config.slotCount()];
        Arrays.setAll(slots, slotIndex -> colours.get(random.nextInt(colours.size())));
        return new Spin(Arrays.asList(slots));
    }
}
