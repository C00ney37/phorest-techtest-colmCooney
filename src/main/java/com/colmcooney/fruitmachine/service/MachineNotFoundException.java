package com.colmcooney.fruitmachine.service;

public class MachineNotFoundException extends RuntimeException {

    public MachineNotFoundException(String machineId) {
        super("Machine not found: " + machineId);
    }
}
