package com.colmcooney.fruitmachine.domain;

/** Thrown when a machine is described in a way that breaks its rules, e.g. two identical colours. */
public class InvalidMachineConfigException extends IllegalArgumentException {

    public InvalidMachineConfigException(String message) {
        super(message);
    }
}
