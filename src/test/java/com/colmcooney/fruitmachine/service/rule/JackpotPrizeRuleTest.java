package com.colmcooney.fruitmachine.service.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JackpotPrizeRuleTest {

    private final MachineConfig config = MachineConfig.classic(100);
    private final JackpotPrizeRule rule = new JackpotPrizeRule();

    @Test
    void paysTheEntireFloatWhenAllSlotsMatch() {
        Spin spin = new Spin(List.of("green", "green", "green", "green"));

        Optional<Prize> prize = rule.evaluate(spin, config, 1_234);

        assertThat(prize).contains(new Prize(PrizeTier.JACKPOT, 1_234));
    }

    @Test
    void doesNotAwardAnythingWhenOneSlotDiffers() {
        Spin spin = new Spin(List.of("green", "green", "green", "white"));

        assertThat(rule.evaluate(spin, config, 1_234)).isEmpty();
    }
}
