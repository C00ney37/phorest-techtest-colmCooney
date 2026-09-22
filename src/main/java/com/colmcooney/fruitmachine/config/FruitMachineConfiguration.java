package com.colmcooney.fruitmachine.config;

import com.colmcooney.fruitmachine.repository.MachineRepository;
import com.colmcooney.fruitmachine.repository.MachineRepositoryImpl;
import com.colmcooney.fruitmachine.service.FruitMachineService;
import com.colmcooney.fruitmachine.service.FruitMachineServiceImpl;
import com.colmcooney.fruitmachine.service.MachineService;
import com.colmcooney.fruitmachine.service.MachineServiceImpl;
import com.colmcooney.fruitmachine.service.Spinner;
import com.colmcooney.fruitmachine.service.SpinnerImpl;
import com.colmcooney.fruitmachine.service.rule.PrizeRules;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the game classes together. They carry no Spring annotations themselves, so all the wiring lives here. */
@Configuration
public class FruitMachineConfiguration {

    @Bean
    public Spinner spinner() {
        return SpinnerImpl.secure();
    }

    @Bean
    public FruitMachineService fruitMachineService(Spinner spinner) {
        return new FruitMachineServiceImpl(spinner, PrizeRules.standard());
    }

    @Bean
    public MachineRepository machineRepository() {
        return new MachineRepositoryImpl();
    }

    @Bean
    public MachineService machineService(
            MachineRepository machineRepository, FruitMachineService fruitMachineService, MeterRegistry meterRegistry) {
        return new MachineServiceImpl(machineRepository, fruitMachineService, meterRegistry);
    }
}
