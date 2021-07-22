package com.jozufozu.flywheel.event;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.multiplayer.ClientLevel;
import com.mojang.math.Matrix4f;
import net.minecraftforge.eventbus.api.Event;

public class RenderLayerEvent extends Event {
	private final ClientLevel world;
	public final RenderType type;
	public final Matrix4f viewProjection;
	public final double camX;
	public final double camY;
	public final double camZ;

	public RenderLayerEvent(ClientLevel world, RenderType type, Matrix4f viewProjection, double camX, double camY, double camZ) {
		this.world = world;
		this.type = type;
		this.viewProjection = viewProjection;
		this.camX = camX;
		this.camY = camY;
		this.camZ = camZ;
	}

	public ClientLevel getWorld() {
		return world;
	}

	public RenderType getType() {
		return type;
	}

	public Matrix4f getViewProjection() {
		return viewProjection;
	}

	public double getCamX() {
		return camX;
	}

	public double getCamY() {
		return camY;
	}

	public double getCamZ() {
		return camZ;
	}
}
