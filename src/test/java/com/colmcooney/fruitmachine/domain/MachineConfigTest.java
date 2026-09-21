package com.colmcooney.fruitmachine.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.colmcooney.fruitmachine.support.Colours;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MachineConfigTest {

    private static final long PLAY_COST = 100;
    private static final int PAIR = 2;

    @Test
    void classicMachineHasFourSlotsFourColoursAndAPairWins() {
        MachineConfig config = MachineConfig.classic(PLAY_COST);

        assertThat(config.slotCount()).isEqualTo(4);
        assertThat(config.colours()).containsExactly("black", "white", "green", "yellow");
        assertThat(config.adjacentMatchLength()).isEqualTo(2);
        assertThat(config.playCost()).isEqualTo(PLAY_COST);
    }

    @Test
    void rejectsFewerThanTwoSlots() {
        assertThatThrownBy(() -> new MachineConfig(1, List.of("black", "white"), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slots");
    }

    @Test
    void rejectsMoreThanTheMaximumNumberOfSlots() {
        assertThatThrownBy(() -> new MachineConfig(MachineConfig.MAX_SLOTS + 1, List.of("black", "white"), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slots");
    }

    @Test
    void acceptsTheMaximumNumberOfSlots() {
        MachineConfig config = new MachineConfig(MachineConfig.MAX_SLOTS, List.of("black", "white"), PAIR, PLAY_COST);

        assertThat(config.slotCount()).isEqualTo(MachineConfig.MAX_SLOTS);
    }

    @Test
    void rejectsFewerThanTwoColours() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black"), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("colours");
    }

    @Test
    void acceptsHundredsOfColours() {
        MachineConfig config = new MachineConfig(4, Colours.numbered(500), PAIR, PLAY_COST);

        assertThat(config.colours()).hasSize(500);
    }

    @Test
    void acceptsTheMaximumNumberOfColours() {
        MachineConfig config = new MachineConfig(4, Colours.numbered(MachineConfig.MAX_COLOURS), PAIR, PLAY_COST);

        assertThat(config.colours()).hasSize(MachineConfig.MAX_COLOURS);
    }

    @Test
    void rejectsMoreThanTheMaximumNumberOfColours() {
        assertThatThrownBy(() -> new MachineConfig(4, Colours.numbered(MachineConfig.MAX_COLOURS + 1), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("colours");
    }

    @Test
    void rejectsBlankColour() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "  "), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void rejectsDuplicateColoursIgnoringCase() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "white", "Black"), PAIR, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, -3})
    void rejectsAnAdjacentMatchLengthBelowTwo(int adjacentMatchLength) {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "white"), adjacentMatchLength, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("adjacent match length");
    }

    @Test
    void rejectsAnAdjacentMatchLengthLongerThanTheMachine() {
        assertThatThrownBy(() -> new MachineConfig(4, List.of("black", "white"), 5, PLAY_COST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("adjacent match length");
    }

    @Test
    void acceptsAnAdjacentMatchLengthEqualToTheSlotCount() {
        MachineConfig config = new MachineConfig(4, List.of("black", "white"), 4, PLAY_COST);

        assertThat(config.adjacentMatchLength()).isEqualTo(4);
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
        MachineConfig config = new MachineConfig(4, colours, PAIR, PLAY_COST);

        colours.add("green");

        assertThat(config.colours()).containsExactly("black", "white");
    }
}
