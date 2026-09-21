package com.colmcooney.fruitmachine.service.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SmallPrizeRuleTest {

    private static final long PLAY_COST = 100;

    private final MachineConfig config = MachineConfig.classic(PLAY_COST);
    private final SmallPrizeRule fiveTimesCost = new SmallPrizeRule(5);

    static List<List<String>> spinsWithAnAdjacentPair() {
        return List.of(
                List.of("black", "black", "white", "green"),
                List.of("black", "white", "white", "green"),
                List.of("black", "white", "green", "green"),
                List.of("black", "black", "white", "white"),
                List.of("black", "black", "black", "white"));
    }

    static List<List<String>> spinsWithoutAnAdjacentPair() {
        return List.of(
                List.of("black", "white", "green", "yellow"),
                List.of("black", "white", "black", "white"),
                List.of("black", "white", "green", "black"));
    }

    @ParameterizedTest
    @MethodSource("spinsWithAnAdjacentPair")
    void paysFiveTimesTheCostWhenTwoAdjacentSlotsMatch(List<String> slots) {
        Optional<Prize> prize = fiveTimesCost.evaluate(new Spin(slots), config, 10_000);

        assertThat(prize).contains(new Prize(PrizeTier.SMALL_PRIZE, 5 * PLAY_COST));
    }

    @ParameterizedTest
    @MethodSource("spinsWithoutAnAdjacentPair")
    void doesNotAwardAnythingWhenMatchingSlotsAreNotAdjacent(List<String> slots) {
        assertThat(fiveTimesCost.evaluate(new Spin(slots), config, 10_000)).isEmpty();
    }

    @Test
    void doesNotDependOnTheSizeOfTheFloat() {
        Spin spin = new Spin(List.of("black", "black", "white", "green"));

        assertThat(fiveTimesCost.evaluate(spin, config, 0)).contains(new Prize(PrizeTier.SMALL_PRIZE, 500));
    }

    @Test
    void rejectsAMultiplierThatIsNotPositive() {
        assertThatThrownBy(() -> new SmallPrizeRule(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
