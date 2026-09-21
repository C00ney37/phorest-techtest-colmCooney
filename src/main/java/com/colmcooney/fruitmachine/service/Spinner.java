package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.Spin;

/** Source of spins. Kept as an interface so tests can script the result instead of relying on chance. */
@FunctionalInterface
public interface Spinner {

    Spin spin(MachineConfig config);
}
