package com.shawn.fraud.application;

public interface CommandHandler<C extends Command, R extends CommandResult> {
    public R execute(C command);
}
