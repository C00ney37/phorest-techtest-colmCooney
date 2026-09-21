package com.colmcooney.fruitmachine.service.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.support.Colours;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class FullHousePrizeRuleTest {

    private final MachineConfig config = MachineConfig.classic(100);
    private final FullHousePrizeRule halfTheFloat = new FullHousePrizeRule(2);

    @Test
    void paysHalfTheFloatWhenEverySlotIsADifferentColour() {
        Spin spin = new Spin(List.of("black", "white", "green", "yellow"));

        Optional<Prize> prize = halfTheFloat.evaluate(spin, config, 1_000);

        assertThat(prize).contains(new Prize(PrizeTier.FULL_HOUSE, 500));
    }

    @Test
    void roundsHalfOfAnOddFloatDown() {
        Spin spin = new Spin(List.of("black", "white", "green", "yellow"));

        Optional<Prize> prize = halfTheFloat.evaluate(spin, config, 1_001);

        assertThat(prize).contains(new Prize(PrizeTier.FULL_HOUSE, 500));
    }

    @Test
    void doesNotAwardAnythingWhenAColourRepeatsEvenIfNotAdjacent() {
        Spin spin = new Spin(List.of("black", "white", "black", "yellow"));

        assertThat(halfTheFloat.evaluate(spin, config, 1_000)).isEmpty();
    }

    @Test
    void aJackpotSpinIsNotAFullHouse() {
        Spin spin = new Spin(List.of("black", "black", "black", "black"));

        assertThat(halfTheFloat.evaluate(spin, config, 1_000)).isEmpty();
    }

    @Test
    void withMoreSlotsThanColoursAColourMustRepeatSoThereIsNeverAFullHouse() {
        MachineConfig manySlots = new MachineConfig(MachineConfig.MAX_SLOTS, Colours.numbered(500), 2, 100);
        Spin spin = new Spin(IntStream.range(0, MachineConfig.MAX_SLOTS)
                .mapToObj(slotNumber -> "colour-" + slotNumber % 500)
                .toList());

        Optional<Prize> prize = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> halfTheFloat.evaluate(spin, manySlots, 1_000));

        assertThat(prize).isEmpty();
    }

    @Test
    void paysAFullHouseWhenEveryOneOfManySlotsShowsADifferentColour() {
        int slotCount = MachineConfig.MAX_COLOURS;
        MachineConfig manyColours = new MachineConfig(slotCount, Colours.numbered(slotCount), 2, 100);
        Spin spin = new Spin(Colours.numbered(slotCount));

        Optional<Prize> prize = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> halfTheFloat.evaluate(spin, manyColours, 1_000));

        assertThat(prize).contains(new Prize(PrizeTier.FULL_HOUSE, 500));
    }

    @Test
    void rejectsADivisorThatIsNotPositive() {
        assertThatThrownBy(() -> new FullHousePrizeRule(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
