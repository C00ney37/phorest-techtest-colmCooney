package com.colmcooney.fruitmachine.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import org.junit.jupiter.api.Test;

class MachineRepositoryImplTest {

    private final MachineRepositoryImpl repository = new MachineRepositoryImpl();

    private static Machine machine(String id, long floatAmount) {
        return new Machine(id, MachineConfig.classic(100), new MachineState(floatAmount, 0));
    }

    @Test
    void findsAMachineThatWasSaved() {
        Machine saved = repository.save(machine("machine-1", 1_000));

        assertThat(repository.findById("machine-1")).contains(saved);
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(repository.findById("no-such-machine")).isEmpty();
    }

    @Test
    void savingAgainReplacesTheEarlierVersion() {
        repository.save(machine("machine-1", 1_000));

        repository.save(machine("machine-1", 2_000));

        assertThat(repository.findById("machine-1").orElseThrow().state().floatAmount()).isEqualTo(2_000);
    }

    @Test
    void keepsMachinesSeparate() {
        repository.save(machine("machine-1", 1_000));
        repository.save(machine("machine-2", 2_000));

        assertThat(repository.findById("machine-1").orElseThrow().state().floatAmount()).isEqualTo(1_000);
        assertThat(repository.findById("machine-2").orElseThrow().state().floatAmount()).isEqualTo(2_000);
    }
}
