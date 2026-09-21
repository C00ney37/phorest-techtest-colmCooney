package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.Spin;

public class FruitMachineServiceImpl implements FruitMachineService {

    private final Spinner spinner;

    public FruitMachineServiceImpl(Spinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public PlayOutcome play(MachineConfig config) {
        Spin spin = spinner.spin(config);
        return new PlayOutcome(spin, spin.isJackpot());
    }
}
