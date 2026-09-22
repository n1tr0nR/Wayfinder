package dev.nitron.wayfinder.client;

/** Converts tracking confidence into a frame, independently of drawing. */
public record SignalIndicator(float lookFactor, int rgb) {
    public static <T> java.util.List<T> targetLast(java.util.List<T> signals, java.util.function.Predicate<T> selected) {
        var result = new java.util.ArrayList<T>(signals.size());
        for (T signal : signals) if (!selected.test(signal)) result.add(signal);
        for (T signal : signals) if (selected.test(signal)) result.add(signal);
        return result;
    }

    public static SignalIndicator resolve(boolean selected, float searchFactor, int signalRgb) {
        return new SignalIndicator(selected ? 1.0F : searchFactor, selected ? signalRgb : 0xFF4E4E);
    }

    public int displacement() {
        return (int) (130.0F * (1.0F - lookFactor));
    }
}
