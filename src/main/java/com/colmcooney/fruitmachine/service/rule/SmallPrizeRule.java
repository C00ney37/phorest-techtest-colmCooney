package com.colmcooney.fruitmachine.service.rule;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.List;
import java.util.Optional;

/** Two adjacent slots show the same colour: pays a multiple of the cost of a play (five times in the original game). */
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
        if (hasAdjacentPair(spin.slots())) {
            return Optional.of(new Prize(PrizeTier.SMALL_PRIZE, costMultiplier * config.playCost()));
        }
        return Optional.empty();
    }

    private static boolean hasAdjacentPair(List<String> slots) {
        for (int slotIndex = 1; slotIndex < slots.size(); slotIndex++) {
            if (slots.get(slotIndex).equals(slots.get(slotIndex - 1))) {
                return true;
            }
        }
        return false;
    }
}
