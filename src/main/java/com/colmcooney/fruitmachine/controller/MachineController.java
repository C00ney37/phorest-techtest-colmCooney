package com.colmcooney.fruitmachine.controller;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.dto.CreateMachineRequest;
import com.colmcooney.fruitmachine.dto.MachineResponse;
import com.colmcooney.fruitmachine.dto.PlayResponse;
import com.colmcooney.fruitmachine.service.MachineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/machines")
@Tag(name = "Machines", description = "Set up fruit machines, read their state and play them")
public class MachineController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    @Operation(
            summary = "Create a machine",
            description = "Sets up a machine with its own float. Slot count, colours and adjacent match length are "
                    + "optional and default to the original game: 4 slots, black/white/green/yellow, a pair wins.")
    @ApiResponse(
            responseCode = "201",
            description = "The machine was created. The Location header points to it.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MachineResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "A field is missing or invalid, or the fields break the machine's rules together "
                    + "(for example duplicate colours, or an adjacent match length longer than the machine).",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MachineResponse> createMachine(@Valid @RequestBody CreateMachineRequest request) {
        Machine machine = machineService.createMachine(request.toConfig(), request.startingFloat());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(machine.id()).toUri();
        return ResponseEntity.created(location).body(MachineResponse.from(machine));
    }

    @GetMapping("/{machineId}")
    @Operation(summary = "Read a machine", description = "Returns the machine's configuration and its current float and free plays.")
    @ApiResponse(responseCode = "200", description = "The machine")
    @ApiResponse(
            responseCode = "404",
            description = "There is no machine with this id.",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public MachineResponse getMachine(@PathVariable String machineId) {
        return MachineResponse.from(machineService.getMachine(machineId));
    }

    @PostMapping("/{machineId}/plays")
    @Operation(
            summary = "Play a machine",
            description = "Spins the machine once and settles the play: takes the stake (or uses a free play), pays any "
                    + "prize from the float, and credits free plays for any part of a prize the float could not cover. "
                    + "Returns the spin, the prize and the machine's totals afterwards.")
    @ApiResponse(responseCode = "200", description = "The outcome of the play")
    @ApiResponse(
            responseCode = "404",
            description = "There is no machine with this id.",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public PlayResponse play(@PathVariable String machineId) {
        return PlayResponse.from(machineService.play(machineId));
    }
}
