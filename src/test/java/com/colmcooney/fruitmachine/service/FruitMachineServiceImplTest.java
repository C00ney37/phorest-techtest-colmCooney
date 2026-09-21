package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.List;
import org.junit.jupiter.api.Test;

class FruitMachineServiceImplTest {

    private final MachineConfig config = MachineConfig.classic();

    private FruitMachineService serviceThatAlwaysSpins(String... colours) {
        Spin scriptedSpin = new Spin(List.of(colours));
        return new FruitMachineServiceImpl(spinConfig -> scriptedSpin);
    }

    @Test
    void playReturnsTheSpin() {
        PlayOutcome outcome = serviceThatAlwaysSpins("black", "white", "green", "yellow").play(config);

        assertThat(outcome.spin().slots()).containsExactly("black", "white", "green", "yellow");
    }

    @Test
    void playReportsAJackpotWhenAllSlotsMatch() {
        PlayOutcome outcome = serviceThatAlwaysSpins("yellow", "yellow", "yellow", "yellow").play(config);

        assertThat(outcome.jackpot()).isTrue();
    }

    @Test
    void playReportsNoJackpotWhenSlotsDiffer() {
        PlayOutcome outcome = serviceThatAlwaysSpins("yellow", "yellow", "yellow", "black").play(config);

        assertThat(outcome.jackpot()).isFalse();
    }
}
