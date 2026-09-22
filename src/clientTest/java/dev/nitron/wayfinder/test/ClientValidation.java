package dev.nitron.wayfinder.test;

import dev.nitron.wayfinder.client.ClientEvents;
import dev.nitron.wayfinder.network.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.CraftingMenu;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.network.ClientSignalStore;
import dev.nitron.wayfinder.registry.*;
import dev.nitron.wayfinder.signal.SignalRecord;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;

@Mod(value = "wayfinder_tests", dist = Dist.CLIENT)
public final class ClientValidation {
    public static boolean shift, control, simulateKeys;
    public static final Set<Integer> pressedKeys = new HashSet<>();
    private final List<String> failures = new ArrayList<>();
    private int networkStep = -1;
    private final long deadline = System.nanoTime() + 300_000_000_000L;
    private boolean started, initialized, capture, sample;
    private BitSet targetMask;
    private int ticks, stage, assertions;
    private BlockPos target;
    private List<SignalRecord> signals;
    private final Minecraft mc = Minecraft.getInstance();
    private static final int STAGES = 27;

    public ClientValidation() {
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::frame);
    }

    private void tick(ClientTickEvent.Post event) {
        if (System.nanoTime() > deadline) { finish("FAILED: client validation timeout"); return; }
        if (!started && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            started = true;
            var rules = new GameRules();
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
            mc.options.pauseOnLostFocus = false;
            mc.createWorldOpenFlows().createFreshLevel("wayfinder-validation",
                    new LevelSettings("Wayfinder validation", GameType.CREATIVE, false,
                            Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(12345, false, false),
                    access -> access.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    new TitleScreen());
        }
        if (mc.player == null || mc.level == null || mc.getOverlay() != null) return;
        if (initialized && networkStep < 0 && stage >= 18) {
            ClientSignalStore.replace(stage % 2 == 0 ? List.of(signals.getFirst()) : signals);
            if (ticks == 19) sample = true;
        }
        if (++ticks < 40) return;
        try {
            if (!initialized) {
                initialized = true;
                target = mc.player.blockPosition().offset(0, 0, 14);
                signals = List.of(signal(target, mc.player.getUUID(), false, 3),
                        signal(target.offset(1, 0, 0), mc.player.getUUID(), false, 3));
                mc.getSingleplayerServer().execute(() -> {
                    var level = mc.getSingleplayerServer().overworld();
                    level.setBlockAndUpdate(target, WayfinderBlocks.SIGNAL_ARRAY.get().defaultBlockState());
                    level.setDayTime(6000);
                });
                runtimeChecks();
                ticks = 0;
                setupNetwork();
                return;
            }
            if (networkStep >= 0) {
                checkNetwork();
                ticks = 0;
                return;
            }
            if (stage >= 18) ClientSignalStore.replace(stage % 2 == 0 ? List.of(signals.getFirst()) : signals);
            capture = true;
        } catch (Throwable failure) {
            failure.printStackTrace();
            finish("FAILED: " + failure);
        }
    }

    private void setupStage() {
        if (stage == 24) signals = List.of(new SignalRecord(target, "Custom", 200, 128, 255, 3, mc.player.getUUID(), false, 0));
        shift = stage < 6 && stage % 3 == 1;
        control = stage < 6 && stage % 3 == 2;
        mc.options.advancedItemTooltips = stage % 2 == 1;
        mc.options.guiScale().set(stage < 18 ? 2 + stage % 3 : 3);
        mc.options.setCameraType(stage == 23 ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
        mc.resizeDisplay();
        ItemStack stack;
        if (stage < 6 || stage >= 18) {
            stack = scope(stage == 0, stage >= 18 ? new double[]{30,32,40}[Math.min(2, (stage - 18) / 2)] : 40);
        } else {
            stack = new ItemStack(WayfinderItems.SIGNAL_TWEAKER.get());
            stack.set(WayfinderDataComponents.SIGNAL_TWEAKER.get(), new SignalTweakerComponent(
                    new Vec3i(76,255,135), stage == 17 ? "Long transmitter name that must wrap without covering controls" : "Signal",
                    stage % 2 == 0, stage % 2 == 0 ? 12 : 3, stage % 3, stage % 6));
        }
        mc.player.getInventory().selected = 0;
        mc.player.getInventory().setItem(0, stack);
        if (stage < 18) {
            mc.gameMode.setLocalMode(GameType.SURVIVAL);
            mc.setScreen(stage < 12 ? new InventoryScene(0) : new TooltipScene(stack, true));
        } else {
            mc.gameMode.setLocalMode(GameType.CREATIVE);
            mc.setScreen(null);
            Vec3 direction = Vec3.atLowerCornerOf(target).subtract(mc.player.getEyePosition());
            mc.player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x, direction.z)));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y, Math.hypot(direction.x,direction.z))));
            if (stage == 25) mc.player.setYRot(mc.player.getYRot() + 45);
            mc.player.yRotO = mc.player.getYRot(); mc.player.xRotO = mc.player.getXRot();
            ClientSignalStore.replace(stage % 2 == 0 ? List.of(signals.getFirst()) : signals);
        }
    }

    private void frame(RenderFrameEvent.Post event) {
        if (!initialized) return;
        if (sample) {
            sample = false;
            try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                targetMask = targetMask(image);
            }
        }
        if (!capture) return;
        capture = false;
        try {
            Path directory = mc.gameDirectory.toPath().resolve("screenshots");
            Files.createDirectories(directory);
            try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                image.writeToFile(directory.resolve(String.format(Locale.ROOT, "%02d.png", stage)));
                if (stage >= 18) {
                    var mask = targetMask(image);
                    check(mask.equals(targetMask), "target pixels stable across frames stage=" + stage);
                    check(stage == 25 ? mask.isEmpty() : mask.cardinality() > 100,
                            "target color follows camera lock stage=" + stage);
                }
            }
            System.out.println("WAYFINDER_CAPTURE stage=" + stage);
            if (++stage == STAGES) finish(failures.isEmpty() ? "PASS assertions=" + assertions + " screenshots=" + STAGES : "FAILED: " + failures);
            else { ticks = 0; setupStage(); }
        } catch (Throwable failure) {
            failure.printStackTrace(); finish("FAILED: " + failure);
        }
    }

    private BitSet targetMask(com.mojang.blaze3d.platform.NativeImage image) {
        var expected = signals.getFirst();
        int rgb = expected.red() | (expected.green() << 8) | (expected.blue() << 16);
        var result = new BitSet();
        int half = (int)(60 * mc.getWindow().getGuiScale());
        for (int y = image.getHeight()/2-half; y < image.getHeight()/2+half; y++) {
            for (int x = image.getWidth()/2-half; x < image.getWidth()/2+half; x++) {
                if ((image.getPixelRGBA(x,y) & 0xFFFFFF) == rgb) result.set(y * image.getWidth() + x);
            }
        }
        return result;
    }

    private void runtimeChecks() {
        var player = mc.player;
        ItemStack scope = scope(true, 40);
        var slot = player.inventoryMenu.getSlot(36);
        slot.set(scope);
        ItemStack[] cursor = {new ItemStack(WayfinderItems.LENS.get())};
        SlotAccess access = new SlotAccess() {
            public ItemStack get() {return cursor[0];}
            public boolean set(ItemStack stack) {cursor[0]=stack;return true;}
        };
        var item = (SignalscopeItem)scope.getItem();
        check(item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access), "insert lens handled");
        check(cursor[0].isEmpty() && component(scope).lensItemstack().is(WayfinderItems.LENS.get()), "one lens transferred");
        cursor[0] = new ItemStack(WayfinderItems.PRIVACY_LENS.get());
        item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access);
        check(cursor[0].is(WayfinderItems.LENS.get()) && component(scope).lensItemstack().is(WayfinderItems.PRIVACY_LENS.get()), "replacement preserves old lens");
        cursor[0]=ItemStack.EMPTY;
        item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access);
        check(cursor[0].is(WayfinderItems.PRIVACY_LENS.get()) && component(scope).lensItemstack().isEmpty(), "remove selected lens");
        cursor[0]=new ItemStack(WayfinderItems.SIGNAL_BOOSTER.get());
        item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access);
        cursor[0]=new ItemStack(WayfinderItems.SIGNAL_EXPANDER.get());
        item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access);
        check(cursor[0].is(WayfinderItems.SIGNAL_BOOSTER.get()) && component(scope).upgradeItemstack().is(WayfinderItems.SIGNAL_EXPANDER.get()), "replacement preserves old upgrade");
        cursor[0]=ItemStack.EMPTY;
        item.overrideOtherStackedOnMe(scope,cursor[0],slot,ClickAction.SECONDARY,player,access);
        check(cursor[0].is(WayfinderItems.SIGNAL_EXPANDER.get()) && component(scope).upgradeItemstack().isEmpty(), "remove selected upgrade");
        UUID other = UUID.fromString("00000000-0000-0000-0000-000000000001");
        for (var lens : List.of(WayfinderItems.LENS.get(),WayfinderItems.PRIVACY_LENS.get(),WayfinderItems.VANTAGE_LENS.get())) {
            scope.set(WayfinderDataComponents.SIGNALSCOPE.get(),new SignalscopeComponent(3,40,new ItemStack(lens),ItemStack.EMPTY,1));
            check(SignalscopeHelper.accepts(player,signal(target,player.getUUID(),false,3),scope) == (lens != WayfinderItems.VANTAGE_LENS.get()), "own lens filter");
            check(SignalscopeHelper.accepts(player,signal(target,other,false,3),scope) == (lens != WayfinderItems.PRIVACY_LENS.get()), "other lens filter");
            check(!SignalscopeHelper.accepts(player,signal(target,other,true,3),scope), "powered private filter");
            check(!SignalscopeHelper.accepts(player,signal(target,player.getUUID(),false,6),scope), "frequency filter");
        }
        int i=0;
        for (ItemStack upgrade : List.of(ItemStack.EMPTY,new ItemStack(WayfinderItems.SIGNAL_BOOSTER.get()),new ItemStack(WayfinderItems.SIGNAL_EXPANDER.get()))) {
            int base = new int[]{1000,1500,2500}[i++];
            for (double gain : new double[]{30,40}) check(SignalscopeHelper.maxDistance(new SignalscopeComponent(3,gain,ItemStack.EMPTY,upgrade,0)) == base * (gain == 30 ? 1 : 2), "range endpoint");
        }
        var originalMenu = player.containerMenu;
        var crafting = new CraftingMenu(1, player.getInventory());
        player.containerMenu = crafting;
        ItemStack result = scope(false, 30);
        crafting.getSlot(0).set(result);
        cursor[0] = ItemStack.EMPTY;
        boolean intercepted = item.overrideOtherStackedOnMe(result, cursor[0], crafting.getSlot(0),
                ClickAction.SECONDARY, player, access);
        check(!intercepted && cursor[0].isEmpty() && !component(result).lensItemstack().isEmpty(),
                "crafting result cannot yield a free lens");
        player.containerMenu = originalMenu;
        System.out.println("WAYFINDER_RUNTIME_ASSERTIONS " + assertions);
    }

    private void setupNetwork() {
        networkStep = 0;
        simulateKeys = true;
        mc.getSingleplayerServer().submit(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            player.getInventory().selected = 0;
            player.getInventory().setItem(0, scope(false, 30));
            ItemStack tweaker = new ItemStack(WayfinderItems.SIGNAL_TWEAKER.get());
            tweaker.set(WayfinderDataComponents.SIGNAL_TWEAKER.get(), new SignalTweakerComponent(
                    new Vec3i(76, 255, 135), "Signal", false, 3, 0, 0));
            player.getInventory().setItem(1, tweaker);
            player.containerMenu.broadcastChanges();
        }).join();
        mc.gameMode.setLocalMode(GameType.SURVIVAL);
        mc.setScreen(new InventoryScene(1));
    }

    private void checkNetwork() {
        var tweaker = mc.player.getInventory().getItem(1).get(WayfinderDataComponents.SIGNAL_TWEAKER.get());
        switch (networkStep++) {
            case 0 -> {
                check(tweaker != null && tweaker.color().getX() == 76, "server inventory reaches client");
                pressedKeys.add(341); // physical left Ctrl, including macOS
                scroll(1);
                pressedKeys.clear();
            }
            case 1 -> {
                check(tweaker.color().getX() == 77, "physical Ctrl scroll changes RGB by one");
                pressedKeys.add(342); // Alt selects fields
                scroll(1);
                pressedKeys.clear();
            }
            case 2 -> {
                check(tweaker.selected() == 5, "Alt scroll wraps field selection");
                scroll(-1);
            }
            case 3 -> {
                check(tweaker.type() == 1, "type edit synchronizes to client");
                PacketDistributor.sendToServer(new SignalscopeSlotPayload(36, 2));
                PacketDistributor.sendToServer(new SignalscopeChangePayload(3, 30, 6, 32));
            }
            case 4 -> {
                var c = component(mc.player.getInventory().getItem(0));
                check(c.frequency() == 6 && c.gain() == 32 && c.selected() == 2, "scope changes synchronize");
                check(!c.lensItemstack().isEmpty() && !c.upgradeItemstack().isEmpty(), "network edits preserve modules");
                simulateKeys = false;
                networkStep = -1;
                if (!failures.isEmpty()) finish("FAILED: " + failures);
                else setupStage();
            }
        }
    }

    private void scroll(double delta) {
        var event = new ScreenEvent.MouseScrolled.Pre(mc.screen, 0, 0, 0, delta);
        ClientEvents.onScreenMouseScroll(event);
        check(event.isCanceled(), "inventory scroll handled");
    }

    private static final class InventoryScene extends InventoryScreen {
        private final int hotbarSlot;
        InventoryScene(int hotbarSlot) { super(Minecraft.getInstance().player); this.hotbarSlot = hotbarSlot; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics, leftPos + 16 + hotbarSlot * 18, topPos + 150, partialTick);
        }
        @Override public boolean isPauseScreen() { return false; }
    }

    private SignalscopeComponent component(ItemStack stack) {return stack.get(WayfinderDataComponents.SIGNALSCOPE.get());}
    private ItemStack scope(boolean empty, double gain) {
        ItemStack stack=new ItemStack(WayfinderItems.SIGNALSCOPE.get());
        stack.set(WayfinderDataComponents.SIGNALSCOPE.get(),new SignalscopeComponent(3,gain,
                empty ? ItemStack.EMPTY : new ItemStack(WayfinderItems.LENS.get()),
                empty ? ItemStack.EMPTY : new ItemStack(WayfinderItems.SIGNAL_BOOSTER.get()),1));
        return stack;
    }
    private SignalRecord signal(BlockPos pos, UUID owner, boolean powered, int frequency) {
        return new SignalRecord(pos,"Signal",76,255,135,frequency,owner,powered,0);
    }
    private void check(boolean ok,String label) {
        assertions++;
        if(!ok) { failures.add(label); System.out.println("WAYFINDER_ASSERTION_FAILED " + label); }
    }
    private void finish(String result) {
        try { Files.writeString(mc.gameDirectory.toPath().resolve("validation.txt"),result); }
        catch(Exception e){throw new RuntimeException(e);}
        System.out.println("WAYFINDER_VALIDATION " + result);
        mc.stop();
    }
    private static final class TooltipScene extends Screen {
        private final ItemStack stack;
        private final boolean edge;
        TooltipScene(ItemStack stack,boolean edge) {super(Component.literal("Tooltip validation"));this.stack=stack;this.edge=edge;}
        public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
            graphics.fill(0,0,width,height,0xFF303840);
            graphics.renderItem(stack,20,20);
            graphics.renderTooltip(font,stack,edge ? width-4 : 45,edge ? height-4 : 45);
        }
    }
}
