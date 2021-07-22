package com.jozufozu.flywheel.backend.instancing;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class InstancedRenderDispatcher {

	private static final WorldAttached<MaterialManager<WorldProgram>> materialManagers = new WorldAttached<>($ -> new MaterialManager<>(Contexts.WORLD));

	private static final WorldAttached<InstanceManager<Entity>> entityInstanceManager = new WorldAttached<>(world -> new EntityInstanceManager(materialManagers.get(world)));
	private static final WorldAttached<InstanceManager<BlockEntity>> tileInstanceManager = new WorldAttached<>(world -> new TileInstanceManager(materialManagers.get(world)));

	@Nonnull
	public static InstanceManager<BlockEntity> getTiles(LevelAccessor world) {
		return tileInstanceManager.get(world);
	}

	@Nonnull
	public static InstanceManager<Entity> getEntities(LevelAccessor world) {
		return entityInstanceManager.get(world);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {

		if (!Backend.isGameActive() || event.phase == TickEvent.Phase.START) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		AnimationTickHolder.tick();

		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		getTiles(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		getEntities(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	public static void enqueueUpdate(BlockEntity te) {
		getTiles(te.getLevel()).queueUpdate(te);
	}

	public static void enqueueUpdate(Entity entity) {
		getEntities(entity.level).queueUpdate(entity);
	}

	@SubscribeEvent
	public static void onBeginFrame(BeginFrameEvent event) {
		materialManagers.get(event.getWorld())
				.checkAndShiftOrigin(event.getInfo());

		getTiles(event.getWorld()).beginFrame(event.getInfo());
		getEntities(event.getWorld()).beginFrame(event.getInfo());
	}

	@SubscribeEvent
	public static void renderLayer(RenderLayerEvent event) {
		ClientLevel world = event.getWorld();
		if (!Backend.getInstance()
				.canUseInstancing(world)) return;

		event.type.setupRenderState();

		materialManagers.get(world)
				.render(event.type, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.clearRenderState();
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientLevel world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			loadAllInWorld(world);
		}
	}

	public static void loadAllInWorld(ClientLevel world) {
		materialManagers.replace(world, MaterialManager::delete);

		InstanceManager<BlockEntity> tiles = tileInstanceManager.replace(world);
		world.blockEntityList.forEach(tiles::add);

		InstanceManager<Entity> entities = entityInstanceManager.replace(world);
		world.entitiesForRendering()
				.forEach(entities::add);
	}
}
