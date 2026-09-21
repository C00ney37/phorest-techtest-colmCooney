package com.colmcooney.fruitmachine.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MachineConfigTest {

    @Test
    void classicMachineHasFourSlotsAndFourColours() {
        MachineConfig config = MachineConfig.classic();

        assertThat(config.slotCount()).isEqualTo(4);
        assertThat(config.colours()).containsExactly("black", "white", "green", "yellow");
    }

    @Test
    void rejectsFewerThanTwoSlots() {
        assertThatThrownBy(() -> new MachineConfig(1, List.of("black", "white")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slots");
    }

    @Test
    void rejectsFewerThanTwoColours() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("colours");
    }

    @Test
    void rejectsBlankColour() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "  ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void rejectsDuplicateColoursIgnoringCase() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "white", "Black")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void takesADefensiveCopyOfTheColours() {
        List<String> colours = new ArrayList<>(List.of("black", "white"));
        MachineConfig config = new MachineConfig(4, colours);

        colours.add("green");

        assertThat(config.colours()).containsExactly("black", "white");
    }
}
