package com.jozufozu.flywheel.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;

public class BeginFrameEvent extends Event {
	private final ClientLevel world;
	private final PoseStack stack;
	private final Camera info;
	private final GameRenderer gameRenderer;
	private final LightTexture lightTexture;

	public BeginFrameEvent(ClientLevel world, PoseStack stack, Camera info, GameRenderer gameRenderer, LightTexture lightTexture) {
		this.world = world;
		this.stack = stack;
		this.info = info;
		this.gameRenderer = gameRenderer;
		this.lightTexture = lightTexture;
	}

	public ClientLevel getWorld() {
		return world;
	}

	public PoseStack getStack() {
		return stack;
	}

	public Camera getInfo() {
		return info;
	}

	public GameRenderer getGameRenderer() {
		return gameRenderer;
	}

	public LightTexture getLightTexture() {
		return lightTexture;
	}
}
