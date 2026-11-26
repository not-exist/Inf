package com.inf.clienttest;

import com.inf.Inf;
import com.inf.block.ForgeBlockEntity;
import com.inf.registry.ModBlocks;
import com.inf.registry.ModItems;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * In-process half of the GUI test.
 *
 * <p>The out-of-process half is {@code tools/gui_driver}, which injects real OS-level
 * input events. This class sets up a world and a forge table, then records whether
 * those events actually reach the game: a click is only accepted when it arrives
 * through {@link ScreenEvent.MouseButtonPressed.Pre}, which is the same path a human
 * click takes. Calling {@code screen.mouseClicked()} directly would bypass GLFW and
 * therefore prove nothing, so it is never done here.
 *
 * <p>Everything is gated on {@code inf.clienttest}, so the class is inert in a normal
 * client run, and the source set is excluded from the release jar by verifyJar.
 */
@EventBusSubscriber(modid = Inf.MODID, value = Dist.CLIENT)
public final class ClientTestHarness {
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("inf.clienttest", "false"));
    // The client runs with its working directory set to the game directory (run/), so the
    // path is relative to that directory, not to the repository root.
    private static final Path STATE_DIR = Path.of(System.getProperty("inf.clienttest.dir", "clienttest"));
    private static final Path REPORT = STATE_DIR.resolve("report.json");
    private static final Path STATE = STATE_DIR.resolve("state.json");
    private static final Path EVENTS = STATE_DIR.resolve("events.log");

    /** A flat block of ground well above the void so the player has something to stand on. */
    private static final BlockPos TABLE = new BlockPos(0, 65, 0);

    private static final long TIMEOUT_TICKS = 20L * 90L;

    private static Stage stage = Stage.IDLE;
    // World and player mutation must happen on the server thread; the harness ticks on the
    // render thread, so the work is handed over with MinecraftServer#execute.
    private static boolean forgeBuildRequested;
    private static volatile boolean forgeBuilt;
    private static int openMenuAttempt;
    private static long worldCreatedAt;
    private static boolean created;
    private static boolean clickSeen;
    private static String clickDetail = "";
    private static String failure;

    private ClientTestHarness() {}

    enum Stage {
        IDLE,
        WAITING_FOR_WORLD,
        PREPARING,
        WAITING_FOR_SCREEN,
        WAITING_FOR_CLICK,
        DONE
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || stage == Stage.DONE) {
            return;
        }
        try {
            tick(Minecraft.getInstance());
        } catch (Throwable t) {
            fail("unhandled exception: " + t);
        }
    }

    private static void tick(Minecraft minecraft) throws IOException {
        if (minecraft == null) {
            return;
        }
        switch (stage) {
            case IDLE -> {
                if (minecraft.screen != null && minecraft.level == null && !created) {
                    created = true;
                    writeState("creating-world");
                    createWorld(minecraft);
                    stage = Stage.WAITING_FOR_WORLD;
                }
            }
            case WAITING_FOR_WORLD -> {
                ServerPlayer player = serverPlayer(minecraft);
                var server = minecraft.getSingleplayerServer();
                if (player != null && server != null && !forgeBuildRequested) {
                    forgeBuildRequested = true;
                    server.execute(() -> {
                        buildForge(player);
                        forgeBuilt = true;
                    });
                }
                if (forgeBuilt) {
                    worldCreatedAt = minecraft.level.getGameTime();
                    stage = Stage.PREPARING;
                    writeState("ready");
                }
            }
            case PREPARING -> {
                if (minecraft.screen instanceof com.inf.client.ForgeScreen) {
                    stage = Stage.WAITING_FOR_CLICK;
                    writeState("screen-open");
                    record("screen", "forge screen opened");
                    return;
                }
                // openMenu is a no-op while the player is still joining, so retry from the
                // server thread until the screen appears instead of once at the ready moment.
                var server = minecraft.getSingleplayerServer();
                if (server != null && openMenuAttempt < 200) {
                    openMenuAttempt++;
                    server.execute(() -> {
                        ServerPlayer player = serverPlayer(minecraft);
                        if (player == null) {
                            record("menu", "no server player yet");
                            return;
                        }
                        if (player.containerMenu != player.inventoryMenu) {
                            return;
                        }
                        if (player.level().getBlockEntity(TABLE) instanceof ForgeBlockEntity forge) {
                            player.openMenu(forge);
                            record("menu", "requested openMenu attempt " + openMenuAttempt);
                        } else {
                            record("menu", "no forge block entity at " + TABLE);
                        }
                    });
                }
                if (expired(minecraft)) {
                    fail("the forge screen never opened");
                }
            }
            case WAITING_FOR_CLICK -> {
                if (clickSeen) {
                    writeState("click-observed");
                    finish(true, "a real click reached the forge screen: " + clickDetail);
                } else if (expired(minecraft)) {
                    fail("no click arrived through ScreenEvent.MouseButtonPressed.Pre");
                }
            }
            default -> {}
        }
    }

    private static boolean expired(Minecraft minecraft) {
        if (worldCreatedAt == 0L || minecraft.level == null) {
            return false;
        }
        return minecraft.level.getGameTime() - worldCreatedAt > TIMEOUT_TICKS;
    }

    private static void createWorld(Minecraft minecraft) {
        LevelSettings settings = new LevelSettings(
                "inf-gui",
                GameType.CREATIVE,
                false,
                Difficulty.PEACEFUL,
                true,
                new GameRules(),
                WorldDataConfiguration.DEFAULT);
        minecraft
                .createWorldOpenFlows()
                .createFreshLevel(
                        "inf-gui",
                        settings,
                        new WorldOptions(0L, false, false),
                        WorldPresets::createNormalWorldDimensions,
                        null);
    }

    /**
     * The integrated server owns the authoritative player; the client-side LocalPlayer
     * cannot place blocks. Look the server player up through the single-player server.
     */
    private static ServerPlayer serverPlayer(Minecraft minecraft) {
        if (minecraft.getSingleplayerServer() == null || minecraft.level == null) {
            return null;
        }
        var server = minecraft.getSingleplayerServer();
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> player.level().dimension() == minecraft.level.dimension())
                .findFirst()
                .orElse(null);
    }

    private static void buildForge(ServerPlayer player) {
        var level = player.serverLevel();
        // A small platform: a flat world can generate nothing but air at this height, and
        // the player must not fall while the driver aims at a fixed screen coordinate.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlockAndUpdate(
                        TABLE.offset(x, -1, z), ModBlocks.FORGE_TABLE.get().defaultBlockState());
            }
        }
        level.setBlockAndUpdate(TABLE, ModBlocks.FORGE_TABLE.get().defaultBlockState());
        if (level.getBlockEntity(TABLE) instanceof ForgeBlockEntity forge) {
            forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_BLADE, new ItemStack(ModItems.SWORD_BLANK.get()));
            forge.inventory().setStackInSlot(ForgeBlockEntity.SLOT_HAMMER, new ItemStack(ModItems.HAMMER.get()));
        }
        player.teleportTo(TABLE.getX() + 0.5, TABLE.getY(), TABLE.getZ() + 2.5);
    }

    /** Arrives only for input that travelled through GLFW, i.e. real OS events. */
    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!ENABLED || !(event.getScreen() instanceof com.inf.client.ForgeScreen)) {
            return;
        }
        clickSeen = true;
        clickDetail = String.format(
                Locale.ROOT, "x=%.1f y=%.1f button=%d", event.getMouseX(), event.getMouseY(), event.getButton());
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (ENABLED) {
            record("key", "code=" + event.getKeyCode());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ENABLED) {
            record("join", event.getEntity().getName().getString());
        }
    }

    /** The GL renderer string proves this ran with a real context (llvmpipe in CI). */
    private static String renderer() {
        try {
            return org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static void record(String kind, String detail) {
        try {
            Files.createDirectories(STATE_DIR);
            Files.writeString(
                    EVENTS,
                    kind + " " + detail + "\n",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // The report is the source of truth; a lost event line is not fatal.
        }
    }

    private static void writeState(String value) throws IOException {
        Files.createDirectories(STATE_DIR);
        Path tmp = STATE.resolveSibling("state.json.tmp");
        Files.writeString(tmp, "{\"stage\":\"" + stage.name() + "\",\"state\":\"" + value + "\"}\n");
        Files.move(tmp, STATE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void fail(String reason) {
        failure = reason;
        finish(false, reason);
    }

    private static void finish(boolean ok, String detail) {
        stage = Stage.DONE;
        try {
            Files.createDirectories(STATE_DIR);
            String gl = renderer();
            String body = String.format(
                    Locale.ROOT,
                    "{\"ok\":%s,\"tier\":\"A\",\"detail\":\"%s\",\"failure\":\"%s\",\"gl\":\"%s\"}\n",
                    ok,
                    detail.replace("\"", "'"),
                    failure == null ? "" : failure.replace("\"", "'"),
                    gl.replace("\"", "'"));
            Files.writeString(REPORT, body);
        } catch (IOException e) {
            failure = "could not write report: " + e;
        }
        Minecraft.getInstance().stop();
    }
}
