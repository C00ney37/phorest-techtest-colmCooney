package com.colmcooney.fruitmachine.service.rule;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.Optional;

/**
 * One way of winning. A rule looks at a spin and either awards a prize or says nothing.
 * Rules are tried in priority order, so a rule need not worry about spins that a higher-priority rule already claims.
 */
@FunctionalInterface
public interface PrizeRule {

    /**
     * @param floatAmount the float at the moment of the spin, after the player's stake has been added
     * @return the prize this spin wins under this rule, or empty if it does not qualify
     */
    Optional<Prize> evaluate(Spin spin, MachineConfig config, long floatAmount);
}
