package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.repository.MachineRepositoryImpl;
import com.colmcooney.fruitmachine.service.rule.PrizeRules;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class MachineServiceImplTest {

    private static final long PLAY_COST = 100;

    private static final List<String> NO_PRIZE = List.of("black", "white", "black", "white");
    private static final List<String> JACKPOT = List.of("yellow", "yellow", "yellow", "yellow");

    private final MachineConfig config = MachineConfig.classic(PLAY_COST);

    private static MachineService serviceShowing(List<String> slots) {
        Spin scriptedSpin = new Spin(slots);
        FruitMachineService fruitMachineService =
                new FruitMachineServiceImpl(spinConfig -> scriptedSpin, PrizeRules.standard());
        return new MachineServiceImpl(new MachineRepositoryImpl(), fruitMachineService, new SimpleMeterRegistry());
    }

    @Test
    void createdMachineHoldsTheStartingFloatAndNoFreePlays() {
        Machine machine = serviceShowing(NO_PRIZE).createMachine(config, 1_000);

        assertThat(machine.config()).isEqualTo(config);
        assertThat(machine.state()).isEqualTo(new MachineState(1_000, 0));
        assertThat(machine.id()).isNotBlank();
    }

    @Test
    void everyCreatedMachineGetsItsOwnId() {
        MachineService service = serviceShowing(NO_PRIZE);

        Machine first = service.createMachine(config, 1_000);
        Machine second = service.createMachine(config, 1_000);

        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void aMachineCanBeReadBackAfterItIsCreated() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine created = service.createMachine(config, 1_000);

        assertThat(service.getMachine(created.id())).isEqualTo(created);
    }

    @Test
    void rejectsANegativeStartingFloat() {
        assertThatThrownBy(() -> serviceShowing(NO_PRIZE).createMachine(config, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gettingAnUnknownMachineFails() {
        assertThatThrownBy(() -> serviceShowing(NO_PRIZE).getMachine("no-such-machine"))
                .isInstanceOf(MachineNotFoundException.class)
                .hasMessageContaining("no-such-machine");
    }

    @Test
    void playingAnUnknownMachineFails() {
        assertThatThrownBy(() -> serviceShowing(NO_PRIZE).play("no-such-machine"))
                .isInstanceOf(MachineNotFoundException.class);
    }

    @Test
    void playStoresTheMachinesNewState() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        PlayOutcome outcome = service.play(machine.id());

        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(1_100, 0));
        assertThat(service.getMachine(machine.id()).state()).isEqualTo(new MachineState(1_100, 0));
    }

    @Test
    void eachPlayStartsFromTheStateTheLastPlayLeft() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        service.play(machine.id());
        PlayOutcome second = service.play(machine.id());

        assertThat(second.stateAfter()).isEqualTo(new MachineState(1_200, 0));
    }

    @Test
    void aJackpotEmptiesTheStoredFloat() {
        MachineService service = serviceShowing(JACKPOT);
        Machine machine = service.createMachine(config, 1_000);

        PlayOutcome outcome = service.play(machine.id());

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.JACKPOT);
        assertThat(service.getMachine(machine.id()).state()).isEqualTo(new MachineState(0, 0));
    }

    @Test
    void playingOneMachineLeavesOthersUntouched() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine played = service.createMachine(config, 1_000);
        Machine untouched = service.createMachine(config, 5_000);

        service.play(played.id());

        assertThat(service.getMachine(untouched.id()).state()).isEqualTo(new MachineState(5_000, 0));
    }

    @Test
    void aPlayDoesNotChangeTheMachineConfig() {
        MachineService service = serviceShowing(NO_PRIZE);
        Machine machine = service.createMachine(config, 1_000);

        service.play(machine.id());

        assertThat(service.getMachine(machine.id()).config()).isEqualTo(config);
    }
}
