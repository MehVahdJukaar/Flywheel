package com.jozufozu.flywheel.backend.instancing;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import com.jozufozu.flywheel.util.RenderUtil;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class InstanceMaterial<D extends InstanceData> {

	protected final Supplier<Vec3i> originCoordinate;
	protected final Cache<Object, Instancer<D>> models;
	protected final MaterialSpec<D> spec;
	private final VertexFormat modelFormat;

	public InstanceMaterial(Supplier<Vec3i> renderer, MaterialSpec<D> spec) {
		this.originCoordinate = renderer;
		this.spec = spec;

		this.models = CacheBuilder.newBuilder()
				.removalListener(notification -> {
					Instancer<?> instancer = (Instancer<?>) notification.getValue();
					RenderWork.enqueue(instancer::delete);
				})
				.build();
		modelFormat = this.spec.getModelFormat();
	}

	public boolean nothingToRender() {
		return models.size() > 0 && models.asMap()
				.values()
				.stream()
				.allMatch(Instancer::empty);
	}

	public void delete() {
		models.invalidateAll();
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		models.asMap()
				.values()
				.forEach(Instancer::clear);
	}

	public void forEachInstancer(Consumer<Instancer<D>> f) {
		for (Instancer<D> model : models.asMap()
				.values()) {
			f.accept(model);
		}
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return get(partial, () -> buildModel(partial.get(), referenceState));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	public Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<PoseStack> modelTransform) {
		return get(Pair.of(dir, partial), () -> buildModel(partial.get(), referenceState, modelTransform.get()));
	}

	public Instancer<D> getModel(BlockState toRender) {
		return get(toRender, () -> buildModel(toRender));
	}

	public Instancer<D> get(Object key, Supplier<BufferedModel> supplier) {
		try {
			return models.get(key, () -> new Instancer<>(supplier.get(), originCoordinate, spec));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedModel buildModel(BlockState renderedState) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();
		return buildModel(dispatcher.getBlockModel(renderedState), renderedState);
	}

	private BufferedModel buildModel(BakedModel model, BlockState renderedState) {
		return buildModel(model, renderedState, new PoseStack());
	}

	private BufferedModel buildModel(BakedModel model, BlockState referenceState, PoseStack ms) {
		BufferBuilderReader reader = new BufferBuilderReader(getBufferBuilder(model, referenceState, ms));

		int vertexCount = reader.getVertexCount();

		ByteBuffer vertices = ByteBuffer.allocate(vertexCount * modelFormat.getStride());
		vertices.order(ByteOrder.nativeOrder());

		for (int i = 0; i < vertexCount; i++) {
			vertices.putFloat(reader.getX(i));
			vertices.putFloat(reader.getY(i));
			vertices.putFloat(reader.getZ(i));

			vertices.put(reader.getNX(i));
			vertices.put(reader.getNY(i));
			vertices.put(reader.getNZ(i));

			vertices.putFloat(reader.getU(i));
			vertices.putFloat(reader.getV(i));
		}

		((Buffer) vertices).rewind();

		// return new BufferedModel(GlPrimitive.QUADS, format, vertices, vertexCount);

		return IndexedModel.fromSequentialQuads(modelFormat, vertices, vertexCount);
	}

	// DOWN, UP, NORTH, SOUTH, WEST, EAST, null
	private static final Direction[] dirs;

	static {
		Direction[] directions = Direction.values();

		dirs = Arrays.copyOf(directions, directions.length + 1);
	}

	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		//		BakedQuadWrapper quadReader = new BakedQuadWrapper();
		//
		//		IModelData modelData = model.getModelData(mc.world, BlockPos.ZERO.up(255), referenceState, VirtualEmptyModelData.INSTANCE);
		//		List<BakedQuad> quads = Arrays.stream(dirs)
		//				.flatMap(dir -> model.getQuads(referenceState, dir, mc.world.rand, modelData).stream())
		//				.collect(Collectors.toList());

		builder.begin(GL11.GL_QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.renderModel(mc.level, model, referenceState, BlockPos.ZERO.above(255), ms, builder, true, mc.level.random, 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
		builder.end();
		return builder;
	}

}
