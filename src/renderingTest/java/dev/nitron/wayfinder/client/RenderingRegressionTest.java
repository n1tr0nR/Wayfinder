package dev.nitron.wayfinder.client;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Dependency-free regression checks; executed by Gradle check. */
public final class RenderingRegressionTest {
    private static int failures;
    private static int checks;

    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"signal_background", "signal_frame"}) {
            try (var stream = RenderingRegressionTest.class.getResourceAsStream(
                    "/assets/wayfinder/textures/gui/sprites/tooltip/" + name + ".png")) {
                BufferedImage texture = ImageIO.read(stream);
                int inset = 0;
                while ((texture.getRGB(inset, texture.getHeight() / 2) >>> 24) == 0) inset++;
                for (int width : new int[]{100, 200, 320}) {
                    var box = TooltipLayout.background(30, 50, width, 100);
                    check(box.x() + inset <= 30 && box.y() + inset <= 50
                                    && box.x() + box.width() - inset >= 30 + width
                                    && box.y() + box.height() - inset >= 150,
                            name + " must enclose content width=" + width);
                }
            }
        }
        for (float searchFactor : new float[]{0.0F, 0.716F, 0.94F, 1.0F}) {
            var locked = SignalIndicator.resolve(true, searchFactor, 0x4CFF99);
            check(locked.displacement() == 0 && locked.lookFactor() == 1.0F
                            && locked.rgb() == 0x4CFF99,
                    "identified target must converge and turn green despite gain offset=" + searchFactor);
        }
        var custom = SignalIndicator.resolve(true, 0.716F, 0x1234AB);
        check(custom.rgb() == 0x1234AB, "lock-on must retain the transmitter's custom color");
        var other = SignalIndicator.resolve(false, 0.716F, 0x1234AB);
        check(other.rgb() == 0xFF4E4E && other.displacement() > 0,
                "unselected signal must retain red searching frames and positional error");
        for (var storeOrder : java.util.List.of(java.util.List.of("target", "other"), java.util.List.of("other", "target"))) {
            var paintOrder = SignalIndicator.targetLast(storeOrder, "target"::equals);
            check(paintOrder.getLast().equals("target") && paintOrder.size() == 2,
                    "overlapping red frames must not paint over the target: " + storeOrder);
        }
        System.out.println("Rendering regression checks: " + checks + ", failures: " + failures);
        if (failures != 0) throw new AssertionError(failures + " rendering checks failed");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            failures++;
            System.err.println("FAIL: " + message);
        }
    }
}
