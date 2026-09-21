package com.colmcooney.fruitmachine.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.domain.PrizeTier;
import com.colmcooney.fruitmachine.domain.Spin;
import com.colmcooney.fruitmachine.service.MachineNotFoundException;
import com.colmcooney.fruitmachine.service.MachineService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Checks the HTTP contract: routes, status codes, JSON shapes, validation and error responses. Game rules are tested elsewhere. */
@WebMvcTest(MachineController.class)
class MachineControllerTest {

    private static final Machine MACHINE = new Machine("machine-1", MachineConfig.classic(100), new MachineState(10_000, 0));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MachineService machineService;

    // Creating a machine

    @Test
    void createsAMachineWithTheOriginalGameAsTheDefault() throws Exception {
        when(machineService.createMachine(MachineConfig.classic(100), 10_000)).thenReturn(MACHINE);

        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playCost\": 100, \"startingFloat\": 10000}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/machines/machine-1"))
                .andExpect(jsonPath("$.id").value("machine-1"))
                .andExpect(jsonPath("$.slotCount").value(4))
                .andExpect(jsonPath("$.colours[0]").value("black"))
                .andExpect(jsonPath("$.adjacentMatchLength").value(2))
                .andExpect(jsonPath("$.playCost").value(100))
                .andExpect(jsonPath("$.floatAmount").value(10_000))
                .andExpect(jsonPath("$.freePlays").value(0));
    }

    @Test
    void passesACustomConfigurationToTheService() throws Exception {
        MachineConfig custom = new MachineConfig(6, List.of("red", "blue", "pink"), 3, 50);
        Machine machine = new Machine("machine-2", custom, new MachineState(500, 0));
        when(machineService.createMachine(custom, 500)).thenReturn(machine);

        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content("""
                        {"slotCount": 6, "colours": ["red", "blue", "pink"], "adjacentMatchLength": 3,
                         "playCost": 50, "startingFloat": 500}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotCount").value(6))
                .andExpect(jsonPath("$.adjacentMatchLength").value(3));

        verify(machineService).createMachine(custom, 500);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            """
            {"startingFloat": 1000} | playCost""",
            """
            {"playCost": 0, "startingFloat": 1000} | playCost""",
            """
            {"playCost": 1000000000001, "startingFloat": 1000} | playCost""",
            """
            {"playCost": 100} | startingFloat""",
            """
            {"playCost": 100, "startingFloat": -1} | startingFloat""",
            """
            {"playCost": 100, "startingFloat": 1000000000001} | startingFloat""",
            """
            {"playCost": 100, "startingFloat": 1000, "slotCount": 1} | slotCount""",
            """
            {"playCost": 100, "startingFloat": 1000, "slotCount": 100001} | slotCount""",
            """
            {"playCost": 100, "startingFloat": 1000, "colours": ["black"]} | colours""",
            """
            {"playCost": 100, "startingFloat": 1000, "colours": ["black", " "]} | colours[1]""",
            """
            {"playCost": 100, "startingFloat": 1000, "adjacentMatchLength": 1} | adjacentMatchLength"""
    })
    void rejectsARequestWithAnInvalidFieldAndSaysWhichOne(String requestBody, String invalidField) throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors['" + invalidField + "']").exists());

        verifyNoInteractions(machineService);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            """
            {"playCost": 100, "startingFloat": 1000, "adjacentMatchLength": 5} | slot count""",
            """
            {"playCost": 100, "startingFloat": 1000, "colours": ["black", "Black"]} | Duplicate colour"""
    })
    void rejectsAMachineWhoseFieldsAreIndividuallyValidButBreakTheRulesTogether(String requestBody, String expectedDetail)
            throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid machine configuration"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString(expectedDetail)));

        verifyNoInteractions(machineService);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            """
            {"playCost": 100, "startingFloat": | malformed JSON""",
            """
            {"playCost": 100, "startingFloat": 1000, "slotCount": "four"} | wrong type""",
            """
            {"playCost": 100, "startingFloat": 1000, "unknownField": 1} | unknown property"""
    })
    void rejectsARequestBodyThatCannotBeReadAsAMachine(String requestBody, String description) throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(machineService);
    }

    @Test
    void rejectsARequestWithNoBody() throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(machineService);
    }

    @Test
    void rejectsARequestThatIsNotJson() throws Exception {
        mockMvc.perform(post("/machines").contentType(MediaType.TEXT_PLAIN).content("playCost=100"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // Reading a machine

    @Test
    void returnsAMachinesConfigurationAndState() throws Exception {
        when(machineService.getMachine("machine-1")).thenReturn(MACHINE);

        mockMvc.perform(get("/machines/machine-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("machine-1"))
                .andExpect(jsonPath("$.slotCount").value(4))
                .andExpect(jsonPath("$.playCost").value(100))
                .andExpect(jsonPath("$.floatAmount").value(10_000))
                .andExpect(jsonPath("$.freePlays").value(0));
    }

    @Test
    void reportsAnUnknownMachineAsNotFound() throws Exception {
        when(machineService.getMachine("nope")).thenThrow(new MachineNotFoundException("nope"));

        mockMvc.perform(get("/machines/nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Machine not found"))
                .andExpect(jsonPath("$.detail").value("Machine not found: nope"));
    }

    // Playing a machine

    @Test
    void returnsTheOutcomeOfAPlay() throws Exception {
        PlayOutcome outcome = new PlayOutcome(
                new Spin(List.of("black", "black", "white", "green")),
                PrizeTier.SMALL_PRIZE, 300, 2, true, new MachineState(0, 2));
        when(machineService.play("machine-1")).thenReturn(outcome);

        mockMvc.perform(post("/machines/machine-1/plays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[0]").value("black"))
                .andExpect(jsonPath("$.slots.length()").value(4))
                .andExpect(jsonPath("$.prizeTier").value("SMALL_PRIZE"))
                .andExpect(jsonPath("$.payout").value(300))
                .andExpect(jsonPath("$.freePlaysCredited").value(2))
                .andExpect(jsonPath("$.usedFreePlay").value(true))
                .andExpect(jsonPath("$.floatAmount").value(0))
                .andExpect(jsonPath("$.freePlays").value(2));
    }

    @Test
    void reportsPlayingAnUnknownMachineAsNotFound() throws Exception {
        when(machineService.play("nope")).thenThrow(new MachineNotFoundException("nope"));

        mockMvc.perform(post("/machines/nope/plays"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Machine not found"));
    }

    // Everything else

    @Test
    void doesNotLeakTheCauseOfAnUnexpectedFailure() throws Exception {
        when(machineService.getMachine("machine-1")).thenThrow(new IllegalStateException("secret internal detail"));

        mockMvc.perform(get("/machines/machine-1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }

    @Test
    void rejectsAMethodThatARouteDoesNotSupport() throws Exception {
        mockMvc.perform(get("/machines/machine-1/plays"))
                .andExpect(status().isMethodNotAllowed());
    }
}
