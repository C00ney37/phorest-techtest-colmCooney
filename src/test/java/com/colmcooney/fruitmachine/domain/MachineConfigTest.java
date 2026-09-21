package com.colmcooney.fruitmachine.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MachineConfigTest {

    private static final long PLAY_COST = 100;

    @Test
    void classicMachineHasFourSlotsAndFourColours() {
        MachineConfig config = MachineConfig.classic(PLAY_COST);

        assertThat(config.slotCount()).isEqualTo(4);
        assertThat(config.colours()).containsExactly("black", "white", "green", "yellow");
        assertThat(config.playCost()).isEqualTo(PLAY_COST);
    }

    @Test
    void rejectsFewerThanTwoSlots() {
        assertThatThrownBy(() -> new MachineConfig(1, List.of("black", "white"), PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slots");
    }

    @Test
    void rejectsFewerThanTwoColours() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black"), PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("colours");
    }

    @Test
    void rejectsBlankColour() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "  "), PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void rejectsDuplicateColoursIgnoringCase() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "white", "Black"), PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rejectsAPlayCostThatIsNotPositive(long playCost) {
        assertThatThrownBy(() -> MachineConfig.classic(playCost))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("play cost");
    }

    @Test
    void takesADefensiveCopyOfTheColours() {
        List<String> colours = new ArrayList<>(List.of("black", "white"));
        MachineConfig config = new MachineConfig(4, colours, PLAY_COST);

        colours.add("green");

        assertThat(config.colours()).containsExactly("black", "white");
    }
}
