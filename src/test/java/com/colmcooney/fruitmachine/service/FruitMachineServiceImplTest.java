package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.service.rule.PrizeRule;
import com.colmcooney.fruitmachine.service.rule.PrizeRules;
import com.colmcooney.fruitmachine.support.Colours;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Money is in minor units. With a play cost of 100 the small prize is 500. */
class FruitMachineServiceImplTest {

    private static final long PLAY_COST = 100;

    private static final List<String> NO_PRIZE = List.of("black", "white", "black", "white");
    private static final List<String> SMALL_PRIZE = List.of("black", "black", "white", "green");
    private static final List<String> FULL_HOUSE = List.of("black", "white", "green", "yellow");
    private static final List<String> JACKPOT = List.of("yellow", "yellow", "yellow", "yellow");

    private final MachineConfig config = MachineConfig.classic(PLAY_COST);

    private static FruitMachineService serviceShowing(List<String> slots) {
        return serviceShowing(slots, PrizeRules.standard());
    }

    private static FruitMachineService serviceShowing(List<String> slots, List<PrizeRule> prizeRules) {
        Spin scriptedSpin = new Spin(slots);
        return new FruitMachineServiceImpl(spinConfig -> scriptedSpin, prizeRules);
    }

    private static MachineState floatOf(long floatAmount) {
        return new MachineState(floatAmount, 0);
    }

    @Test
    void reportsWhatTheSlotsShowed() {
        PlayOutcome outcome = serviceShowing(FULL_HOUSE).play(config, floatOf(1_000));

        assertThat(outcome.spin().slots()).containsExactlyElementsOf(FULL_HOUSE);
    }

    @Test
    void aLosingPlayAddsTheStakeToTheFloat() {
        PlayOutcome outcome = serviceShowing(NO_PRIZE).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.NONE);
        assertThat(outcome.payout()).isZero();
        assertThat(outcome.usedFreePlay()).isFalse();
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(1_100, 0));
    }

    @Test
    void aJackpotPaysTheEntireFloatIncludingTheStake() {
        PlayOutcome outcome = serviceShowing(JACKPOT).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.JACKPOT);
        assertThat(outcome.payout()).isEqualTo(1_100);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 0));
    }

    @Test
    void aJackpotOnAnEmptyFloatNeverCreditsFreePlays() {
        PlayOutcome outcome = serviceShowing(JACKPOT).play(config, floatOf(0));

        assertThat(outcome.payout()).isEqualTo(PLAY_COST);
        assertThat(outcome.freePlaysCredited()).isZero();
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 0));
    }

    @Test
    void aFullHousePaysHalfTheFloat() {
        PlayOutcome outcome = serviceShowing(FULL_HOUSE).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.FULL_HOUSE);
        assertThat(outcome.payout()).isEqualTo(550);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(550, 0));
    }

    @Test
    void aFullHouseRoundsHalfTheFloatDownAndTheMachineKeepsTheOddUnit() {
        PlayOutcome outcome = serviceShowing(FULL_HOUSE).play(config, floatOf(1_001));

        assertThat(outcome.payout()).isEqualTo(550);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(551, 0));
    }

    @Test
    void aSmallPrizePaysFiveTimesTheCost() {
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.SMALL_PRIZE);
        assertThat(outcome.payout()).isEqualTo(500);
        assertThat(outcome.freePlaysCredited()).isZero();
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(600, 0));
    }

    @Test
    void aSmallPrizeThatExactlyEmptiesTheFloatNeedsNoFreePlays() {
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, floatOf(400));

        assertThat(outcome.payout()).isEqualTo(500);
        assertThat(outcome.freePlaysCredited()).isZero();
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 0));
    }

    @Test
    void aSmallPrizeTheFloatCannotCoverPaysWhatIsLeftAndCreditsTheShortfallAsFreePlays() {
        // Float after stake is 300, prize is 500: the shortfall of 200 is two plays' worth.
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, floatOf(200));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.SMALL_PRIZE);
        assertThat(outcome.payout()).isEqualTo(300);
        assertThat(outcome.freePlaysCredited()).isEqualTo(2);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 2));
    }

    @Test
    void aShortfallThatIsNotAWholeNumberOfPlaysIsRoundedUpToTheNextFreePlay() {
        // Float after stake is 150, prize is 500: the shortfall of 350 is 3.5 plays.
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, floatOf(50));

        assertThat(outcome.payout()).isEqualTo(150);
        assertThat(outcome.freePlaysCredited()).isEqualTo(4);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 4));
    }

    @Test
    void aJackpotTakesPrecedenceOverTheSmallPrizeItAlsoQualifiesFor() {
        PlayOutcome outcome = serviceShowing(JACKPOT).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.JACKPOT);
    }

    @Test
    void aFreePlayIsUsedInsteadOfPayingTheStake() {
        PlayOutcome outcome = serviceShowing(NO_PRIZE).play(config, new MachineState(1_000, 2));

        assertThat(outcome.usedFreePlay()).isTrue();
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(1_000, 1));
    }

    @Test
    void aWinOnAFreePlayIsStillPaid() {
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, new MachineState(1_000, 1));

        assertThat(outcome.usedFreePlay()).isTrue();
        assertThat(outcome.payout()).isEqualTo(500);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(500, 0));
    }

    @Test
    void aFreePlayThatWinsMoreThanTheFloatCanPayCreditsFreePlaysAfterUsingTheOneItSpent() {
        PlayOutcome outcome = serviceShowing(SMALL_PRIZE).play(config, new MachineState(0, 1));

        assertThat(outcome.payout()).isZero();
        assertThat(outcome.freePlaysCredited()).isEqualTo(5);
        assertThat(outcome.stateAfter()).isEqualTo(new MachineState(0, 5));
    }

    @Test
    void doesNotChangeTheStateItWasGiven() {
        MachineState before = floatOf(1_000);

        serviceShowing(JACKPOT).play(config, before);

        assertThat(before).isEqualTo(new MachineState(1_000, 0));
    }

    @Test
    void usesTheFirstMatchingRuleInPriorityOrder() {
        PrizeRule higherPriority = (spin, ruleConfig, floatAmount) -> Optional.of(new Prize(PrizeTier.FULL_HOUSE, 7));
        PrizeRule lowerPriority = (spin, ruleConfig, floatAmount) -> Optional.of(new Prize(PrizeTier.SMALL_PRIZE, 9));

        PlayOutcome outcome = serviceShowing(NO_PRIZE, List.of(higherPriority, lowerPriority)).play(config, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.FULL_HOUSE);
        assertThat(outcome.payout()).isEqualTo(7);
    }

    @Test
    void aNewRuleCanBeAddedWithoutChangingTheService() {
        PrizeRule paysWhenTheLastSlotIsYellow = (spin, ruleConfig, floatAmount) ->
                spin.slots().getLast().equals("yellow") ? Optional.of(new Prize(PrizeTier.SMALL_PRIZE, 250)) : Optional.empty();

        PlayOutcome outcome = serviceShowing(FULL_HOUSE, List.of(paysWhenTheLastSlotIsYellow)).play(config, floatOf(1_000));

        assertThat(outcome.payout()).isEqualTo(250);
    }

    // Generalised machines: variable slot count, many colours, and k adjacent slots to win.

    @Test
    void withAMatchLengthOfThreeAPairPaysNothing() {
        MachineConfig threeInARow = new MachineConfig(6, List.of("black", "white", "green", "yellow"), 3, PLAY_COST);

        PlayOutcome outcome = serviceShowing(List.of("black", "black", "white", "white", "green", "green"))
                .play(threeInARow, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.NONE);
    }

    @Test
    void withAMatchLengthOfThreeARunOfThreeWinsTheSmallPrize() {
        MachineConfig threeInARow = new MachineConfig(6, List.of("black", "white", "green", "yellow"), 3, PLAY_COST);

        PlayOutcome outcome = serviceShowing(List.of("black", "white", "white", "white", "green", "yellow"))
                .play(threeInARow, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.SMALL_PRIZE);
        assertThat(outcome.payout()).isEqualTo(500);
    }

    @Test
    void whenTheMatchLengthEqualsTheSlotCountAFullRowIsAJackpotNotASmallPrize() {
        MachineConfig fullRowNeeded = new MachineConfig(4, List.of("black", "white", "green", "yellow"), 4, PLAY_COST);

        PlayOutcome outcome = serviceShowing(JACKPOT).play(fullRowNeeded, floatOf(1_000));

        assertThat(outcome.prizeTier()).isEqualTo(PrizeTier.JACKPOT);
    }

    @Test
    void playsAMaximumSizeMachineWithHundredsOfColours() {
        MachineConfig largeConfig = new MachineConfig(MachineConfig.MAX_SLOTS, Colours.numbered(500), 3, PLAY_COST);
        FruitMachineService service = new FruitMachineServiceImpl(new SpinnerImpl(new Random(42)), PrizeRules.standard());

        PlayOutcome outcome = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> service.play(largeConfig, floatOf(1_000)));

        assertThat(outcome.spin().slots()).hasSize(MachineConfig.MAX_SLOTS);
        assertThat(outcome.stateAfter().floatAmount()).isNotNegative();
    }
}
