package com.colmcooney.fruitmachine.api;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.service.MachineService;
import jakarta.validation.Valid;
import java.net.URI;
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
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    public ResponseEntity<MachineResponse> createMachine(@Valid @RequestBody CreateMachineRequest request) {
        Machine machine = machineService.createMachine(request.toConfig(), request.startingFloat());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(machine.id()).toUri();
        return ResponseEntity.created(location).body(MachineResponse.from(machine));
    }

    @GetMapping("/{machineId}")
    public MachineResponse getMachine(@PathVariable String machineId) {
        return MachineResponse.from(machineService.getMachine(machineId));
    }

    @PostMapping("/{machineId}/plays")
    public PlayResponse play(@PathVariable String machineId) {
        return PlayResponse.from(machineService.play(machineId));
    }
}
