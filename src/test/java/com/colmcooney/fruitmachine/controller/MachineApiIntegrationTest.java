package com.colmcooney.fruitmachine.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.colmcooney.fruitmachine.support.Colours;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Drives the real application (real wiring, real random spins) through HTTP, checking the responses stay consistent. */
@SpringBootTest
@AutoConfigureMockMvc
class MachineApiIntegrationTest {

    private static final long PLAY_COST = 100;
    // A small float, so that small prizes regularly outrun it and free plays are credited and then used.
    private static final long STARTING_FLOAT = 300;

    @Autowired
    private MockMvc mockMvc;

    private String createMachine(String requestBody) throws Exception {
        String location = mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String readMachine(String machineId) throws Exception {
        return mockMvc.perform(get("/machines/" + machineId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String play(String machineId) throws Exception {
        return mockMvc.perform(post("/machines/" + machineId + "/plays"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private static long numberAt(String json, String path) {
        Number number = JsonPath.read(json, path);
        return number.longValue();
    }

    @Test
    void aNewMachineCanBeReadBackWithTheOriginalGameAsItsDefaults() throws Exception {
        String machineId = createMachine("{\"playCost\": 100, \"startingFloat\": 10000}");

        String machine = readMachine(machineId);

        assertThat(numberAt(machine, "$.slotCount")).isEqualTo(4);
        assertThat(numberAt(machine, "$.adjacentMatchLength")).isEqualTo(2);
        assertThat(numberAt(machine, "$.playCost")).isEqualTo(PLAY_COST);
        assertThat(numberAt(machine, "$.floatAmount")).isEqualTo(10_000);
        assertThat(numberAt(machine, "$.freePlays")).isZero();
        List<String> colours = JsonPath.read(machine, "$.colours");
        assertThat(colours).containsExactly("black", "white", "green", "yellow");
    }

    @Test
    void everyPlayMovesMoneyAndFreePlaysConsistentlyAndTheMachineReportsTheSameTotals() throws Exception {
        String machineId = createMachine("{\"playCost\": 100, \"startingFloat\": " + STARTING_FLOAT + "}");
        long expectedFloat = STARTING_FLOAT;
        long expectedFreePlays = 0;

        for (int playNumber = 0; playNumber < 200; playNumber++) {
            String outcome = play(machineId);

            List<String> slots = JsonPath.read(outcome, "$.slots");
            assertThat(slots).hasSize(4).isSubsetOf("black", "white", "green", "yellow");
            boolean usedFreePlay = JsonPath.read(outcome, "$.usedFreePlay");
            expectedFloat = expectedFloat + (usedFreePlay ? 0 : PLAY_COST) - numberAt(outcome, "$.payout");
            expectedFreePlays = expectedFreePlays - (usedFreePlay ? 1 : 0) + numberAt(outcome, "$.freePlaysCredited");
            assertThat(numberAt(outcome, "$.floatAmount")).isEqualTo(expectedFloat);
            assertThat(numberAt(outcome, "$.freePlays")).isEqualTo(expectedFreePlays);
        }

        String machine = readMachine(machineId);
        assertThat(numberAt(machine, "$.floatAmount")).isEqualTo(expectedFloat);
        assertThat(numberAt(machine, "$.freePlays")).isEqualTo(expectedFreePlays);
    }

    @Test
    void aMachineWithManySlotsAndHundredsOfColoursCanBeCreatedAndPlayed() throws Exception {
        String colours = Colours.numbered(500).stream().map(colour -> "\"" + colour + "\"").collect(Collectors.joining(","));
        String machineId = createMachine("""
                {"slotCount": 1000, "colours": [%s], "adjacentMatchLength": 3, "playCost": 100, "startingFloat": 5000}"""
                .formatted(colours));

        String outcome = play(machineId);

        List<String> slots = JsonPath.read(outcome, "$.slots");
        assertThat(slots).hasSize(1000);
    }

    @Test
    void machinesAreIndependentOfEachOther() throws Exception {
        String playedMachineId = createMachine("{\"playCost\": 100, \"startingFloat\": 10000}");
        String untouchedMachineId = createMachine("{\"playCost\": 100, \"startingFloat\": 20000}");

        play(playedMachineId);

        assertThat(numberAt(readMachine(untouchedMachineId), "$.floatAmount")).isEqualTo(20_000);
    }

    @Test
    void anUnknownMachineIsNotFound() throws Exception {
        mockMvc.perform(get("/machines/no-such-machine")).andExpect(status().isNotFound());
        mockMvc.perform(post("/machines/no-such-machine/plays")).andExpect(status().isNotFound());
    }

    @Test
    void anInvalidMachineIsRejectedAndNothingIsCreated() throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playCost\": -5, \"startingFloat\": 100}"))
                .andExpect(status().isBadRequest());
    }
}
