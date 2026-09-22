package com.colmcooney.fruitmachine.service;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.repository.MachineRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineServiceImpl implements MachineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MachineServiceImpl.class);

    private final MachineRepository machineRepository;
    private final FruitMachineService fruitMachineService;
    private final MeterRegistry meterRegistry;

    /**
     * One lock per machine. A play reads the machine, works out its new state and writes it back, so two plays
     * on the same machine must not interleave. Plays on different machines do not wait for each other.
     * This only protects a single running instance; with more than one, the store would need optimistic locking.
     */
    private final Map<String, Object> locksByMachineId = new ConcurrentHashMap<>();

    public MachineServiceImpl(MachineRepository machineRepository, FruitMachineService fruitMachineService, MeterRegistry meterRegistry) {
        this.machineRepository = machineRepository;
        this.fruitMachineService = fruitMachineService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Machine createMachine(MachineConfig config, long startingFloat) {
        MachineState startingState = new MachineState(startingFloat, 0);
        Machine machine = new Machine(UUID.randomUUID().toString(), config, startingState);
        return machineRepository.save(machine);
    }

    @Override
    public Machine getMachine(String machineId) {
        return machineRepository.findById(machineId).orElseThrow(() -> new MachineNotFoundException(machineId));
    }

    @Override
    public PlayOutcome play(String machineId) {
        // Checked before taking a lock so that made-up ids do not each leave a lock object behind.
        getMachine(machineId);

        synchronized (locksByMachineId.computeIfAbsent(machineId, machineWithoutALock -> new Object())) {
            Machine machine = getMachine(machineId);
            PlayOutcome outcome = fruitMachineService.play(machine.config(), machine.state());
            machineRepository.save(machine.withState(outcome.stateAfter()));
            recordPlay(machineId, outcome);
            return outcome;
        }
    }

    private void recordPlay(String machineId, PlayOutcome outcome) {
        String tier = outcome.prizeTier().name();
        LOGGER.info(
                "Play settled: machineId={}, tier={}, payout={}, floatAfter={}",
                machineId, tier, outcome.payout(), outcome.stateAfter().floatAmount());

        meterRegistry.counter("fruitmachine.plays", "tier", tier).increment();
        meterRegistry.counter("fruitmachine.payouts", "tier", tier).increment(outcome.payout());
    }
}
