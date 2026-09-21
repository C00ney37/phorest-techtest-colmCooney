package com.colmcooney.fruitmachine.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class SpinTest {

    @Test
    void allSlotsTheSameColourIsAJackpot() {
        assertThat(new Spin(List.of("green", "green", "green", "green")).isJackpot()).isTrue();
    }

    @Test
    void oneDifferentSlotIsNotAJackpotWhereverItSits() {
        assertThat(new Spin(List.of("white", "green", "green", "green")).isJackpot()).isFalse();
        assertThat(new Spin(List.of("green", "white", "green", "green")).isJackpot()).isFalse();
        assertThat(new Spin(List.of("green", "green", "green", "white")).isJackpot()).isFalse();
    }

    @Test
    void allDifferentIsNotAJackpot() {
        assertThat(new Spin(List.of("black", "white", "green", "yellow")).isJackpot()).isFalse();
    }

    @Test
    void rejectsAnEmptySpin() {
        assertThatThrownBy(() -> new Spin(List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
