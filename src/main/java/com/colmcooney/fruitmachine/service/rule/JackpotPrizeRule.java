package com.colmcooney.fruitmachine.service.rule;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.Optional;

/** All slots show the same colour: pays out the entire float. */
public class JackpotPrizeRule implements PrizeRule {

    @Override
    public Optional<Prize> evaluate(Spin spin, MachineConfig config, long floatAmount) {
        if (spin.isJackpot()) {
            return Optional.of(new Prize(PrizeTier.JACKPOT, floatAmount));
        }
        return Optional.empty();
    }
}
