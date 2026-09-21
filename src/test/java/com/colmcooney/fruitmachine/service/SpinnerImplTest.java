package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SpinnerImplTest {

    private static final long SEED = 42L;

    private final MachineConfig config = MachineConfig.classic(100);
    private final SpinnerImpl spinner = new SpinnerImpl(new Random(SEED));

    @Test
    void spinShowsOneColourPerSlot() {
        assertThat(spinner.spin(config).slots()).hasSize(config.slotCount());
    }

    @Test
    void spinOnlyShowsConfiguredColours() {
        for (int spinNumber = 0; spinNumber < 1_000; spinNumber++) {
            assertThat(spinner.spin(config).slots()).isSubsetOf(config.colours());
        }
    }

    @Test
    void everySlotCanShowEveryColour() {
        Set<String> coloursSeenInFirstSlot = new HashSet<>();
        Set<String> coloursSeenInLastSlot = new HashSet<>();

        for (int spinNumber = 0; spinNumber < 1_000; spinNumber++) {
            Spin spin = spinner.spin(config);
            coloursSeenInFirstSlot.add(spin.slots().getFirst());
            coloursSeenInLastSlot.add(spin.slots().getLast());
        }

        assertThat(coloursSeenInFirstSlot).containsExactlyInAnyOrderElementsOf(config.colours());
        assertThat(coloursSeenInLastSlot).containsExactlyInAnyOrderElementsOf(config.colours());
    }

    /** With 4 slots and 4 colours a jackpot has probability 4 / 4^4 = 1 in 64. The seed keeps this deterministic. */
    @Test
    void jackpotFrequencyMatchesTheExpectedOddsOfOneIn64() {
        int spins = 64_000;
        int jackpots = 0;

        for (int spinNumber = 0; spinNumber < spins; spinNumber++) {
            if (spinner.spin(config).isJackpot()) {
                jackpots++;
            }
        }

        assertThat(jackpots).isBetween(800, 1_200);
    }
}
