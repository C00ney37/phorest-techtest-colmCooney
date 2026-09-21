package com.colmcooney.fruitmachine.domain;

/** A machine that has been set up: its fixed rules plus its current float and free plays. */
public record Machine(String id, MachineConfig config, MachineState state) {

    public Machine withState(MachineState newState) {
        return new Machine(id, config, newState);
    }
}
