package com.example.myceliumminer;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class MyceliumMinerClient implements ClientModInitializer {
    public static final List<Vec3> ROUTE = new ArrayList<>();
    public static final List<BlockPos> JUMP_POINTS = new ArrayList<>();
    public static final List<BlockPos> MINE_POINTS = new ArrayList<>();

    public static boolean recording = false;
    public static boolean running = false;
    private static int routeIndex = 0;
    private static int mineIndex = 0;
    private static int jumpCooldown = 0;
    private static int recordCooldown = 0;
    private static BlockPos lastJumpPoint = null;

    private static KeyMapping openMenuKey;
    private static KeyMapping toggleKey;
    private static KeyMapping recordKey;
    private static Vec3 lastRecorded = null;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("myceliumminer", "controls"));

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.myceliumminer.menu", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.myceliumminer.toggle", InputConstants.Type.KEYSYM,
                InputConstants.KEY_F6, category));
        recordKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.myceliumminer.record", InputConstants.Type.KEYSYM,
                InputConstants.KEY_F7, category));

        ClientTickEvents.END_CLIENT_TICK.register(MyceliumMinerClient::tick);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            releaseMovement(client);
            return;
        }

        while (openMenuKey.consumeClick()) {
            client.setScreen(new MinerScreen());
        }
        while (toggleKey.consumeClick()) {
            running = !running;
            client.player.displayClientMessage(Component.literal(
                    running ? "Mycelium Miner: running" : "Mycelium Miner: paused"), true);
        }
        while (recordKey.consumeClick()) {
            recording = !recording;
            lastRecorded = null;
            client.player.displayClientMessage(Component.literal(
                    recording ? "Route recording started" : "Route recording stopped"), true);
        }

        if (recording && recordCooldown-- <= 0) {
            recordCooldown = 10;
            Vec3 here = client.player.position();
            if (lastRecorded == null || here.distanceTo(lastRecorded) >= 1.25) {
                ROUTE.add(here);
                lastRecorded = here;
                client.player.displayClientMessage(Component.literal(
                        "Recorded route point " + ROUTE.size()), true);
            }
        }

        if (!running) {
            releaseMovement(client);
            return;
        }
        if (ROUTE.isEmpty()) {
            running = false;
            client.player.displayClientMessage(Component.literal(
                    "Add route points or record a route first."), true);
            return;
        }

        Vec3 playerPos = client.player.position();
        Vec3 waypoint = ROUTE.get(routeIndex % ROUTE.size());
        double dx = waypoint.x - playerPos.x;
        double dz = waypoint.z - playerPos.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // Face the next waypoint and walk towards it.
        if (horizontalDistance > 0.35) {
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            client.player.setYRot(yaw);
            client.player.setYHeadRot(yaw);
            client.options.keyUp.setDown(true);
        } else {
            client.options.keyUp.setDown(false);
            routeIndex = (routeIndex + 1) % ROUTE.size();
        }

        // Trigger a short jump when the player enters a configured jump radius.
        if (jumpCooldown > 0) jumpCooldown--;
        if (jumpCooldown == 0) client.options.keyJump.setDown(false);
        boolean nearPreviousJump = lastJumpPoint != null
                && playerPos.distanceTo(Vec3.atCenterOf(lastJumpPoint)) <= 2.0;
        if (!nearPreviousJump) lastJumpPoint = null;
        if (jumpCooldown == 0) {
            for (BlockPos point : JUMP_POINTS) {
                if (lastJumpPoint == null
                        && playerPos.distanceTo(Vec3.atCenterOf(point)) <= 1.15) {
                    client.options.keyJump.setDown(true);
                    jumpCooldown = 2;
                    lastJumpPoint = point;
                    break;
                }
            }
        }

        // Mine configured block positions when they are close enough.
        if (!MINE_POINTS.isEmpty()) {
            if (mineIndex >= MINE_POINTS.size()) mineIndex = 0;
            BlockPos target = MINE_POINTS.get(mineIndex);
            if (playerPos.distanceTo(Vec3.atCenterOf(target)) <= 4.5) {
                Vec3 eye = client.player.getEyePosition();
                Vec3 aim = Vec3.atCenterOf(target).subtract(eye);
                double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
                float targetYaw = (float) (Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90.0);
                float targetPitch = (float) -Math.toDegrees(Math.atan2(aim.y, horizontal));
                client.player.setYRot(targetYaw);
                client.player.setYHeadRot(targetYaw);
                client.player.setXRot(targetPitch);

                if (client.level.getBlockState(target).isAir()) {
                    mineIndex++;
                    client.options.keyAttack.setDown(false);
                } else if (client.hitResult instanceof BlockHitResult hit
                        && hit.getBlockPos().equals(target)) {
                    client.options.keyAttack.setDown(true);
                } else {
                    // Do not accidentally mine a different block if the target is obstructed.
                    client.options.keyAttack.setDown(false);
                }
            } else {
                client.options.keyAttack.setDown(false);
            }
        } else {
            client.options.keyAttack.setDown(false);
        }
    }

    private static void releaseMovement(Minecraft client) {
        if (client.options == null) return;
        client.options.keyUp.setDown(false);
        client.options.keyJump.setDown(false);
        client.options.keyAttack.setDown(false);
    }

    public static void resetRouteProgress() {
        routeIndex = 0;
        mineIndex = 0;
        jumpCooldown = 0;
        lastJumpPoint = null;
    }
}
