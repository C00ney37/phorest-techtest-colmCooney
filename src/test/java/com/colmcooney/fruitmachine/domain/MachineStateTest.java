package com.colmcooney.fruitmachine.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MachineStateTest {

    @Test
    void acceptsAnEmptyFloatAndNoFreePlays() {
        MachineState state = new MachineState(0, 0);

        assertThat(state.floatAmount()).isZero();
        assertThat(state.freePlays()).isZero();
    }

    @Test
    void rejectsANegativeFloat() {
        assertThatThrownBy(() -> new MachineState(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("float");
    }

    @Test
    void rejectsNegativeFreePlays() {
        assertThatThrownBy(() -> new MachineState(0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Free plays");
    }
}
