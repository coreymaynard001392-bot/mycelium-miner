
package com.example.myceliumminer;

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
    private String status = "Enter coordinates below.";

    public MinerScreen() {
        super(Component.literal("Mycelium Miner"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int fieldWidth = 60;

        xBox = new EditBox(
            this.font, cx - 100, 55, fieldWidth, 20,
            Component.literal("X")
        );
        yBox = new EditBox(
            this.font, cx - 30, 55, fieldWidth, 20,
            Component.literal("Y")
        );
        zBox = new EditBox(
            this.font, cx + 40, 55, fieldWidth, 20,
            Component.literal("Z")
        );

        xBox.setHint(Component.literal("X"));
        yBox.setHint(Component.literal("Y"));
        zBox.setHint(Component.literal("Z"));

        xBox.setMaxLength(8);
        yBox.setMaxLength(8);
        zBox.setMaxLength(8);

        addRenderableWidget(xBox);
        addRenderableWidget(yBox);
        addRenderableWidget(zBox);

        int left = cx - 155;
        int right = cx + 5;
        int buttonWidth = 150;

        addRenderableWidget(Button.builder(
            Component.literal("Add route point"),
            b -> addPoint(0)
        ).bounds(left, 90, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Add jump point"),
            b -> addPoint(1)
        ).bounds(right, 90, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Add mining target"),
            b -> addPoint(2)
        ).bounds(left, 116, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Record route"),
            b -> {
                MyceliumMinerClient.recording =
                    !MyceliumMinerClient.recording;
                status = MyceliumMinerClient.recording
                    ? "Recording started."
                    : "Recording stopped.";
            }
        ).bounds(right, 116, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Clear route"),
            b -> {
                MyceliumMinerClient.ROUTE.clear();
                MyceliumMinerClient.resetRouteProgress();
                status = "Route cleared.";
            }
        ).bounds(left, 142, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Start / Pause"),
            b -> {
                MyceliumMinerClient.running =
                    !MyceliumMinerClient.running;

                if (MyceliumMinerClient.running) {
                    MyceliumMinerClient.resetRouteProgress();
                }

                status = MyceliumMinerClient.running
                    ? "Automation enabled."
                    : "Automation paused.";
            }
        ).bounds(right, 142, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Done"),
            b -> onClose()
        ).bounds(cx - 50, this.height - 30, 100, 20).build());
    }

    private void addPoint(int type) {
        try {
            int x = Integer.parseInt(xBox.getValue().trim());
            int y = Integer.parseInt(yBox.getValue().trim());
            int z = Integer.parseInt(zBox.getValue().trim());

            BlockPos pos = new BlockPos(x, y, z);

            if (type == 0) {
                MyceliumMinerClient.ROUTE.add(pos.getCenter());
                status = "Route point added.";
            } else if (type == 1) {
                MyceliumMinerClient.JUMP_POINTS.add(pos);
                status = "Jump point added.";
            } else {
                MyceliumMinerClient.MINE_POINTS.add(pos);
                status = "Mining target added.";
            }

        } catch (NumberFormatException ex) {
            status = "Enter valid whole-number coordinates.";
        }
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        // Keep the simple rendering approach from the working test screen.
        super.render(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;

        graphics.drawCenteredString(
            this.font, "MYCELIUM MINER",
            cx, 20, 0xFFFFFF
        );

        graphics.drawCenteredString(
            this.font, "Enter X / Y / Z coordinates",
            cx, 40, 0xAAAAAA
        );

        graphics.drawString(
            this.font,
            "Route points: " + MyceliumMinerClient.ROUTE.size(),
            cx - 155, 177, 0xFFFFFF
        );

        graphics.drawString(
            this.font,
            "Jump points: " + MyceliumMinerClient.JUMP_POINTS.size(),
            cx - 155, 191, 0xFFFFFF
        );

        graphics.drawString(
            this.font,
            "Mining targets: " + MyceliumMinerClient.MINE_POINTS.size(),
            cx - 155, 205, 0xFFFFFF
        );

        graphics.drawString(
            this.font,
            "Status: " + status,
            cx - 155, 225, 0xFFFF55
        );

        graphics.drawString(
            this.font,
            "F6 = Start/Pause | F7 = Record",
            cx - 155, 245, 0xAAAAAA
        );
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
