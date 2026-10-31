package com.example.myceliumminer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MinerScreen extends Screen {

    public MinerScreen() {
        super(Component.literal("Mycelium Miner Test"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredString(
            this.font,
            "MYCELIUM MINER IS WORKING",
            this.width / 2,
            this.height / 2,
            0xFFFFFF
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
