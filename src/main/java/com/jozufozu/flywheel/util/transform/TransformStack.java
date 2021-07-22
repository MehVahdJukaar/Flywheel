package com.jozufozu.flywheel.util.transform;

import net.minecraft.core.Direction;
import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;
import net.minecraft.core.Vec3i;

public interface TransformStack {
	Vec3 CENTER = new Vec3(0.5, 0.5, 0.5);

	TransformStack translate(double x, double y, double z);

	TransformStack multiply(Quaternion quaternion);

	TransformStack push();

	TransformStack pop();

	default TransformStack rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		return multiply(axis.step()
				.rotation(radians));
	}

	default TransformStack rotate(double angle, Direction.Axis axis) {
		Vector3f vec =
				axis == Direction.Axis.X ? Vector3f.XP : axis == Direction.Axis.Y ? Vector3f.YP : Vector3f.ZP;
		return multiply(vec, angle);
	}

	default TransformStack rotateX(double angle) {
		return multiply(Vector3f.XP, angle);
	}

	default TransformStack rotateY(double angle) {
		return multiply(Vector3f.YP, angle);
	}

	default TransformStack rotateZ(double angle) {
		return multiply(Vector3f.ZP, angle);
	}

	default TransformStack centre() {
		return translate(CENTER);
	}

	default TransformStack unCentre() {
		return translateBack(CENTER);
	}

	default TransformStack translate(Vec3i vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	default TransformStack translate(Vec3 vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	default TransformStack translateBack(Vec3 vec) {
		return translate(-vec.x, -vec.y, -vec.z);
	}

	default TransformStack nudge(int id) {
		long randomBits = (long) id * 31L * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		return translate(xNudge, yNudge, zNudge);
	}

	default TransformStack multiply(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		return multiply(axis.rotationDegrees((float) angle));
	}
}
