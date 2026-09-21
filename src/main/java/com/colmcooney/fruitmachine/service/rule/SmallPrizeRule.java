package com.colmcooney.fruitmachine.service.rule;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.List;
import java.util.Optional;

/**
 * A run of adjacent slots of the same colour, at least as long as the machine's adjacent match length, pays a multiple
 * of the cost of a play (five times in the original game). However many runs there are, the prize is paid once.
 */
public class SmallPrizeRule implements PrizeRule {

    private final long costMultiplier;

    public SmallPrizeRule(long costMultiplier) {
        if (costMultiplier <= 0) {
            throw new IllegalArgumentException("The cost multiplier must be positive, got " + costMultiplier);
        }
        this.costMultiplier = costMultiplier;
    }

    @Override
    public Optional<Prize> evaluate(Spin spin, MachineConfig config, long floatAmount) {
        if (hasRunOfAtLeast(spin.slots(), config.adjacentMatchLength())) {
            return Optional.of(new Prize(PrizeTier.SMALL_PRIZE, costMultiplier * config.playCost()));
        }
        return Optional.empty();
    }

    /** One pass over the slots, tracking the length of the current run, so the cost does not grow with the run length. */
    private static boolean hasRunOfAtLeast(List<String> slots, int runLength) {
        int currentRun = 1;
        for (int slotIndex = 1; slotIndex < slots.size(); slotIndex++) {
            if (slots.get(slotIndex).equals(slots.get(slotIndex - 1))) {
                currentRun++;
                if (currentRun >= runLength) {
                    return true;
                }
            } else {
                currentRun = 1;
            }
        }
        return false;
    }
}
