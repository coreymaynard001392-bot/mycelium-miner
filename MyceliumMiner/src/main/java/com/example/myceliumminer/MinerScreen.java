package com.example.myceliumminer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

public class MinerScreen extends Screen {
    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private String status = "Enter block/player coordinates below.";

    public MinerScreen() {
        super(Component.literal("Mycelium Miner"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        xBox = new EditBox(this.font, cx - 105, 62, 62, 20, Component.literal("X"));
        yBox = new EditBox(this.font, cx - 31, 62, 62, 20, Component.literal("Y"));
        zBox = new EditBox(this.font, cx + 43, 62, 62, 20, Component.literal("Z"));
        xBox.setHint(Component.literal("X"));
        yBox.setHint(Component.literal("Y"));
        zBox.setHint(Component.literal("Z"));
        xBox.setMaxLength(8);
        yBox.setMaxLength(8);
        zBox.setMaxLength(8);
        addRenderableWidget(xBox);
        addRenderableWidget(yBox);
        addRenderableWidget(zBox);

        addRenderableWidget(Button.builder(Component.literal("Add route point"), b -> addPoint(0))
                .bounds(cx - 155, 96, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Add jump point"), b -> addPoint(1))
                .bounds(cx + 5, 96, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Add mining target"), b -> addPoint(2))
                .bounds(cx - 155, 122, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Record current route"), b -> {
            MyceliumMinerClient.recording = !MyceliumMinerClient.recording;
            status = MyceliumMinerClient.recording ? "Recording route: walk your loop." : "Recording stopped.";
        }).bounds(cx + 5, 122, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Clear route"), b -> {
            MyceliumMinerClient.ROUTE.clear();
            MyceliumMinerClient.resetRouteProgress();
            status = "Route cleared.";
        }).bounds(cx - 155, 148, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Start / Pause"), b -> {
            MyceliumMinerClient.running = !MyceliumMinerClient.running;
            if (MyceliumMinerClient.running) MyceliumMinerClient.resetRouteProgress();
            status = MyceliumMinerClient.running ? "Automation enabled." : "Automation paused.";
        }).bounds(cx + 5, 148, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 50, this.height - 30, 100, 20).build());
    }

    private void addPoint(int type) {
        try {
            int x = Integer.parseInt(xBox.getValue().trim());
            int y = Integer.parseInt(yBox.getValue().trim());
            int z = Integer.parseInt(zBox.getValue().trim());
            BlockPos pos = new BlockPos(x, y, z);
            if (type == 0) {
                MyceliumMinerClient.ROUTE.add(pos.getCenter());
                status = "Added route point at " + x + ", " + y + ", " + z;
            } else if (type == 1) {
                MyceliumMinerClient.JUMP_POINTS.add(pos);
                status = "Added jump point at " + x + ", " + y + ", " + z;
            } else {
                MyceliumMinerClient.MINE_POINTS.add(pos);
                status = "Added mining target at " + x + ", " + y + ", " + z;
            }
        } catch (NumberFormatException ex) {
            status = "Please enter whole-number X, Y and Z coordinates.";
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        int cx = this.width / 2;
        graphics.drawCenteredString(this.font, "MYCELIUM MINER", cx, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "X / Y / Z coordinates", cx, 48, 0xAAAAAA);
        graphics.drawString(this.font, "Route points: " + MyceliumMinerClient.ROUTE.size(), cx - 155, 180, 0xFFFFFF);
        graphics.drawString(this.font, "Jump points: " + MyceliumMinerClient.JUMP_POINTS.size(), cx - 155, 194, 0xFFFFFF);
        graphics.drawString(this.font, "Mining targets: " + MyceliumMinerClient.MINE_POINTS.size(), cx - 155, 208, 0xFFFFFF);
        graphics.drawString(this.font, "Status: " + status, cx - 155, 230, 0xFFFF55);
        graphics.drawString(this.font, "F6 = start/pause | F7 = record toggle | Right Shift = menu",
                cx - 155, 250, 0xAAAAAA);
        graphics.drawString(this.font, "Single-player prototype. Test in a copied world.",
                cx - 155, 264, 0xFFAA55);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
