package com.jozufozu.flywheel.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum BooleanConfig {
	ENGINE(() -> BooleanConfig::enabled),
	NORMAL_OVERLAY(() -> BooleanConfig::normalOverlay),
	;

	final Supplier<Consumer<BooleanDirective>> receiver;

	BooleanConfig(Supplier<Consumer<BooleanDirective>> receiver) {
		this.receiver = receiver;
	}

	public SConfigureBooleanPacket packet(BooleanDirective directive) {
		return new SConfigureBooleanPacket(this, directive);
	}

	@OnlyIn(Dist.CLIENT)
	private static void enabled(BooleanDirective state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Flywheel Renderer is currently: ").append(boolToText(FlwConfig.get().client.enabled.get()));
			player.displayClientMessage(text, false);
			return;
		}

		boolean enabled = state.get();
		boolean cannotUseER = OptifineHandler.usingShaders() && enabled;

		FlwConfig.get().client.enabled.set(enabled);

		Component text = boolToText(FlwConfig.get().client.enabled.get()).append(new TextComponent(" Flywheel Renderer").withStyle(ChatFormatting.WHITE));
		Component error = new TextComponent("Flywheel Renderer does not support Optifine Shaders").withStyle(ChatFormatting.RED);

		player.displayClientMessage(cannotUseER ? error : text, false);
		Backend.reloadWorldRenderers();
	}

	@OnlyIn(Dist.CLIENT)
	private static void normalOverlay(BooleanDirective state) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || state == null) return;

		if (state == BooleanDirective.DISPLAY) {
			Component text = new TextComponent("Normal overlay is currently: ").append(boolToText(FlwConfig.get().client.normalDebug.get()));
			player.displayClientMessage(text, false);
			return;
		}

		FlwConfig.get().client.normalDebug.set(state.get());

		Component text = boolToText(FlwConfig.get().client.normalDebug.get()).append(new TextComponent(" Normal Overlay").withStyle(ChatFormatting.WHITE));

		player.displayClientMessage(text, false);
	}

	private static MutableComponent boolToText(boolean b) {
		return b ? new TextComponent("enabled").withStyle(ChatFormatting.DARK_GREEN) : new TextComponent("disabled").withStyle(ChatFormatting.RED);
	}
}
