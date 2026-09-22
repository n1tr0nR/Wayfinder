package dev.nitron.wayfinder.client;

/** Geometry shared by tooltip painting and its measured content. */
public final class TooltipLayout {
    public record Rect(int x, int y, int width, int height) {}

    public static Rect background(int x, int y, int width, int height) {
        return new Rect(x - 12, y - 12, width + 24, height + 24);
    }

    private TooltipLayout() {}
}
