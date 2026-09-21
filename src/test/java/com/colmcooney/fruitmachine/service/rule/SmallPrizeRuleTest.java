package com.colmcooney.fruitmachine.service.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SmallPrizeRuleTest {

    private static final long PLAY_COST = 100;
    private static final List<String> COLOURS = List.of("black", "white", "green", "yellow");

    private final SmallPrizeRule fiveTimesCost = new SmallPrizeRule(5);

    private static MachineConfig configWithMatchLength(int slotCount, int adjacentMatchLength) {
        return new MachineConfig(slotCount, COLOURS, adjacentMatchLength, PLAY_COST);
    }

    private static Spin spinOf(String... slots) {
        return new Spin(List.of(slots));
    }

    /** {@code count} copies of {@code colour}, for building long spins. */
    private static List<String> repeated(String colour, int count) {
        return Collections.nCopies(count, colour);
    }

    /** A spin made of the given stretches of slots, one after another. */
    @SafeVarargs
    private static Spin joined(List<String>... parts) {
        List<String> slots = new ArrayList<>();
        for (List<String> part : parts) {
            slots.addAll(part);
        }
        return new Spin(slots);
    }

    // The original game: a pair of adjacent slots wins.

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
        Optional<Prize> prize = fiveTimesCost.evaluate(new Spin(slots), MachineConfig.classic(PLAY_COST), 10_000);

        assertThat(prize).contains(new Prize(PrizeTier.SMALL_PRIZE, 5 * PLAY_COST));
    }

    @ParameterizedTest
    @MethodSource("spinsWithoutAnAdjacentPair")
    void doesNotAwardAnythingWhenMatchingSlotsAreNotAdjacent(List<String> slots) {
        assertThat(fiveTimesCost.evaluate(new Spin(slots), MachineConfig.classic(PLAY_COST), 10_000)).isEmpty();
    }

    @Test
    void doesNotDependOnTheSizeOfTheFloat() {
        Spin spin = spinOf("black", "black", "white", "green");

        assertThat(fiveTimesCost.evaluate(spin, MachineConfig.classic(PLAY_COST), 0))
                .contains(new Prize(PrizeTier.SMALL_PRIZE, 500));
    }

    // Generalised: k adjacent slots win, where k comes from the machine's config.

    @Test
    void withAMatchLengthOfThreeARunOfThreeWinsWhereverItSits() {
        MachineConfig config = configWithMatchLength(6, 3);

        assertThat(fiveTimesCost.evaluate(spinOf("black", "black", "black", "white", "green", "yellow"), config, 1_000)).isPresent();
        assertThat(fiveTimesCost.evaluate(spinOf("white", "black", "black", "black", "green", "yellow"), config, 1_000)).isPresent();
        assertThat(fiveTimesCost.evaluate(spinOf("white", "green", "yellow", "black", "black", "black"), config, 1_000)).isPresent();
    }

    @Test
    void withAMatchLengthOfThreeARunOfTwoIsNotEnough() {
        MachineConfig config = configWithMatchLength(6, 3);

        assertThat(fiveTimesCost.evaluate(spinOf("black", "black", "white", "white", "green", "green"), config, 1_000)).isEmpty();
    }

    @Test
    void withAMatchLengthOfThreeThreeMatchingSlotsThatAreNotAdjacentDoNotWin() {
        MachineConfig config = configWithMatchLength(6, 3);

        assertThat(fiveTimesCost.evaluate(spinOf("black", "white", "black", "green", "black", "yellow"), config, 1_000)).isEmpty();
    }

    @Test
    void aRunLongerThanTheMatchLengthStillWins() {
        MachineConfig config = configWithMatchLength(6, 3);

        assertThat(fiveTimesCost.evaluate(spinOf("black", "black", "black", "black", "black", "white"), config, 1_000)).isPresent();
    }

    @Test
    void severalQualifyingRunsStillPayTheSmallPrizeOnlyOnce() {
        MachineConfig config = configWithMatchLength(6, 3);

        Optional<Prize> prize =
                fiveTimesCost.evaluate(spinOf("black", "black", "black", "white", "white", "white"), config, 1_000);

        assertThat(prize).contains(new Prize(PrizeTier.SMALL_PRIZE, 500));
    }

    @Test
    void whenTheMatchLengthEqualsTheSlotCountOnlyAFullRowWins() {
        MachineConfig config = configWithMatchLength(4, 4);

        assertThat(fiveTimesCost.evaluate(spinOf("black", "black", "black", "black"), config, 1_000)).isPresent();
        assertThat(fiveTimesCost.evaluate(spinOf("black", "black", "black", "white"), config, 1_000)).isEmpty();
    }

    // Scale: the check must stay linear in the number of slots, even for a large match length.

    @Test
    void findsARunAtTheVeryEndOfAMaximumSizeMachine() {
        MachineConfig config = configWithMatchLength(MachineConfig.MAX_SLOTS, 3);
        List<String> alternating = new ArrayList<>();
        for (int slotNumber = 0; slotNumber < MachineConfig.MAX_SLOTS - 3; slotNumber++) {
            alternating.add(slotNumber % 2 == 0 ? "black" : "white");
        }
        Spin spin = joined(alternating, repeated("green", 3));

        Optional<Prize> prize =
                assertTimeoutPreemptively(Duration.ofSeconds(5), () -> fiveTimesCost.evaluate(spin, config, 1_000));

        assertThat(prize).isPresent();
    }

    @Test
    void staysFastWhenTheMatchLengthIsHalfTheMachine() {
        int halfTheMachine = MachineConfig.MAX_SLOTS / 2;
        MachineConfig config = configWithMatchLength(MachineConfig.MAX_SLOTS, halfTheMachine);
        // The first run is one slot too short, so a scan that re-checked a window of slots at every position would be slow.
        Spin spin = joined(repeated("black", halfTheMachine - 1), repeated("white", 1), repeated("black", halfTheMachine));

        Optional<Prize> prize =
                assertTimeoutPreemptively(Duration.ofSeconds(5), () -> fiveTimesCost.evaluate(spin, config, 1_000));

        assertThat(prize).isPresent();
    }

    @Test
    void doesNotAwardARunOneShortOfAHugeMatchLength() {
        int halfTheMachinePlusOne = MachineConfig.MAX_SLOTS / 2 + 1;
        MachineConfig config = configWithMatchLength(MachineConfig.MAX_SLOTS, halfTheMachinePlusOne);
        Spin spin = joined(
                repeated("black", halfTheMachinePlusOne - 2), repeated("white", 1), repeated("black", halfTheMachinePlusOne - 1));

        Optional<Prize> prize =
                assertTimeoutPreemptively(Duration.ofSeconds(5), () -> fiveTimesCost.evaluate(spin, config, 1_000));

        assertThat(prize).isEmpty();
    }

    @Test
    void rejectsAMultiplierThatIsNotPositive() {
        assertThatThrownBy(() -> new SmallPrizeRule(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
