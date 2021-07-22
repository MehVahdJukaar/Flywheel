package com.jozufozu.flywheel.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;

public class ReloadRenderersEvent extends Event {
	private final ClientLevel world;

	public ReloadRenderersEvent(ClientLevel world) {
		this.world = world;
	}

	public ClientLevel getWorld() {
		return world;
	}
}
