package com.fireos8.settingshub;

public final class RootResult {

    public final int exitCode;
    public final String output;
    public final boolean success;

    public RootResult(int exitCode, String output, boolean success) {
        this.exitCode = exitCode;
        this.output = output;
        this.success = success;
    }
}
