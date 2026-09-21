package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.Prize;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.service.rule.PrizeRule;
import java.util.List;
import java.util.Optional;

public class FruitMachineServiceImpl implements FruitMachineService {

    private final Spinner spinner;
    private final List<PrizeRule> prizeRules;

    /** @param prizeRules tried in order; the first rule that awards a prize wins */
    public FruitMachineServiceImpl(Spinner spinner, List<PrizeRule> prizeRules) {
        this.spinner = spinner;
        this.prizeRules = List.copyOf(prizeRules);
    }

    @Override
    public PlayOutcome play(MachineConfig config, MachineState state) {
        // A play is paid for with a free play if the player has one, otherwise with the stake.
        // Either way the spin is settled against the float as it stands once the stake is in.
        boolean usedFreePlay = state.freePlays() > 0;
        long stake = usedFreePlay ? 0 : config.playCost();
        long freePlaysRemaining = usedFreePlay ? state.freePlays() - 1 : state.freePlays();
        long floatAmount = state.floatAmount() + stake;

        Spin spin = spinner.spin(config);
        Prize prize = findPrize(spin, config, floatAmount);

        long payout = Math.min(prize.amount(), floatAmount);
        long shortfall = prize.amount() - payout;
        long freePlaysCredited = Math.ceilDiv(shortfall, config.playCost());

        MachineState stateAfter = new MachineState(floatAmount - payout, freePlaysRemaining + freePlaysCredited);
        return new PlayOutcome(spin, prize.tier(), payout, freePlaysCredited, usedFreePlay, stateAfter);
    }

    private Prize findPrize(Spin spin, MachineConfig config, long floatAmount) {
        for (PrizeRule prizeRule : prizeRules) {
            Optional<Prize> prize = prizeRule.evaluate(spin, config, floatAmount);
            if (prize.isPresent()) {
                return prize.get();
            }
        }
        return Prize.none();
    }
}
