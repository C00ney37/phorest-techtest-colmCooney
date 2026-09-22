package com.colmcooney.fruitmachine.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.colmcooney.fruitmachine.domain.Machine;
import com.colmcooney.fruitmachine.domain.MachineConfig;
import com.colmcooney.fruitmachine.domain.MachineState;
import com.colmcooney.fruitmachine.domain.PlayOutcome;
import com.colmcooney.fruitmachine.repository.MachineRepositoryImpl;
import com.colmcooney.fruitmachine.service.rule.PrizeRules;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class MachineServiceImplConcurrencyTest {

    private static final long PLAY_COST = 100;
    // A small float, so that small prizes regularly outrun it and free plays are credited and then used.
    private static final long STARTING_FLOAT = 300;
    private static final int THREADS = 16;
    private static final int PLAYS_PER_THREAD = 500;

    /**
     * Whatever the spins turn out to be, money and free plays must balance exactly:
     * final float = starting float + stakes paid - payouts, and final free plays = credited - used.
     * If two plays ever read the same state and one overwrote the other, these totals would not add up.
     */
    @Test
    void concurrentPlaysOnOneMachineNeverLoseAnUpdate() throws Exception {
        FruitMachineService fruitMachineService = new FruitMachineServiceImpl(SpinnerImpl.secure(), PrizeRules.standard());
        MachineService service = new MachineServiceImpl(new MachineRepositoryImpl(), fruitMachineService, new SimpleMeterRegistry());
        Machine machine = service.createMachine(MachineConfig.classic(PLAY_COST), STARTING_FLOAT);

        List<PlayOutcome> outcomes = playConcurrently(service, machine.id());

        long stakesPaid = outcomes.stream().filter(outcome -> !outcome.usedFreePlay()).count() * PLAY_COST;
        long paidOut = outcomes.stream().mapToLong(PlayOutcome::payout).sum();
        long freePlaysCredited = outcomes.stream().mapToLong(PlayOutcome::freePlaysCredited).sum();
        long freePlaysUsed = outcomes.stream().filter(PlayOutcome::usedFreePlay).count();

        MachineState finalState = service.getMachine(machine.id()).state();
        assertThat(outcomes).hasSize(THREADS * PLAYS_PER_THREAD);
        assertThat(finalState.floatAmount()).isEqualTo(STARTING_FLOAT + stakesPaid - paidOut);
        assertThat(finalState.freePlays()).isEqualTo(freePlaysCredited - freePlaysUsed);
    }

    private static List<PlayOutcome> playConcurrently(MachineService service, String machineId) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startTogether = new CountDownLatch(1);
        List<Future<List<PlayOutcome>>> playersResults = new ArrayList<>();
        try {
            for (int player = 0; player < THREADS; player++) {
                playersResults.add(executor.submit(() -> {
                    startTogether.await();
                    List<PlayOutcome> playerOutcomes = new ArrayList<>();
                    for (int playNumber = 0; playNumber < PLAYS_PER_THREAD; playNumber++) {
                        playerOutcomes.add(service.play(machineId));
                    }
                    return playerOutcomes;
                }));
            }
            startTogether.countDown();

            List<PlayOutcome> allOutcomes = new ArrayList<>();
            for (Future<List<PlayOutcome>> playerResults : playersResults) {
                allOutcomes.addAll(playerResults.get());
            }
            return allOutcomes;
        } finally {
            executor.shutdownNow();
        }
    }
}
