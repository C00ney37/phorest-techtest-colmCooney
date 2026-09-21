package com.colmcooney.fruitmachine.service.rule;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Every slot shows a different colour: pays out a fraction of the float (half in the original game). */
public class FullHousePrizeRule implements PrizeRule {

    private final long floatDivisor;

    /** @param floatDivisor the float is divided by this to get the prize, so 2 means half; rounds down */
    public FullHousePrizeRule(long floatDivisor) {
        if (floatDivisor <= 0) {
            throw new IllegalArgumentException("The float divisor must be positive, got " + floatDivisor);
        }
        this.floatDivisor = floatDivisor;
    }

    @Override
    public Optional<Prize> evaluate(Spin spin, MachineConfig config, long floatAmount) {
        if (allDistinct(spin.slots())) {
            return Optional.of(new Prize(PrizeTier.FULL_HOUSE, floatAmount / floatDivisor));
        }
        return Optional.empty();
    }

    private static boolean allDistinct(List<String> slots) {
        Set<String> coloursSeen = new HashSet<>();
        for (String colour : slots) {
            if (!coloursSeen.add(colour)) {
                return false;
            }
        }
        return true;
    }
}
