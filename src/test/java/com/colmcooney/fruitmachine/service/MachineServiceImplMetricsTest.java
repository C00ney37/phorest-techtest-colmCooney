package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.repository.MachineRepositoryImpl;
import com.colmcooney.fruitmachine.service.rule.PrizeRules;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class MachineServiceImplMetricsTest {

    private static final long PLAY_COST = 100;
    private static final List<String> SMALL_PRIZE = List.of("black", "black", "white", "green");
    private static final List<String> NO_PRIZE = List.of("black", "white", "black", "white");

    private final MachineConfig config = MachineConfig.classic(PLAY_COST);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private MachineService serviceShowing(List<String> slots) {
        Spin scriptedSpin = new Spin(slots);
        FruitMachineService fruitMachineService = new FruitMachineServiceImpl(spinConfig -> scriptedSpin, PrizeRules.standard());
        return new MachineServiceImpl(new MachineRepositoryImpl(), fruitMachineService, meterRegistry);
    }

    @Test
    void countsAPlayUnderItsPrizeTier() {
        MachineService service = serviceShowing(SMALL_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        service.play(machine.id());

        assertThat(meterRegistry.counter("fruitmachine.plays", "tier", "SMALL_PRIZE").count()).isEqualTo(1.0);
        assertThat(meterRegistry.counter("fruitmachine.plays", "tier", "NONE").count()).isZero();
    }

    @Test
    void addsThePayoutToTheCounterForThatTier() {
        MachineService service = serviceShowing(SMALL_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        service.play(machine.id());
        service.play(machine.id());

        assertThat(meterRegistry.counter("fruitmachine.payouts", "tier", "SMALL_PRIZE").count()).isEqualTo(1_000.0);
    }

    @Test
    void aLosingPlayIsCountedWithNoPayout() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        service.play(machine.id());

        assertThat(meterRegistry.counter("fruitmachine.plays", "tier", "NONE").count()).isEqualTo(1.0);
        assertThat(meterRegistry.counter("fruitmachine.payouts", "tier", "NONE").count()).isZero();
    }
}
