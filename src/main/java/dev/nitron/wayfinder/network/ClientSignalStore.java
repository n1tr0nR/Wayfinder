package dev.nitron.wayfinder.network;

import dev.nitron.wayfinder.signal.SignalRecord;

import java.util.List;

public final class ClientSignalStore {
    private static volatile List<SignalRecord> signals = List.of();

    public static List<SignalRecord> signals() {
        return signals;
    }

    public static void replace(List<SignalRecord> next) {
        signals = List.copyOf(next);
    }

    public static void clear() {
        signals = List.of();
    }

    private ClientSignalStore() {}
}
