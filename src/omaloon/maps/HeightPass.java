package omaloon.maps;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;

/**
 * a pass for custom height for a planet mesh
 */
public abstract class HeightPass {
	public abstract float height(Vec3 pos, float height);

	/**
	 * A pass for creating craters defined by a sphere.
	 * offset sets the offset that the crater creates.
	 * set defines if the offset increments or sets the height of the mesh.
	 */
	public static class CraterHeight extends HeightPass {
		public Vec3 position;
		public float radius, offset;
		public boolean set = false;

		public CraterHeight(Vec3 position, float radius, float offset) {
			this.position = position;
			this.radius = radius;
			this.offset = offset;
		}

		@Override
		public float height(Vec3 pos, float height) {
			if (pos.dst(position) < radius) return offset + height * (set ? 0f : 1f);
			return height;
		}
	}
	/**
	 * A pass for creating a noisy terrain. That uses simplex Noise.
	 * offset is a noise position offset.
	 * heightOffset is the noise result offset.
	 * @see Simplex
	 */
	public static class NoiseHeight extends HeightPass {
		public Vec3 offset = new Vec3();
		public int seed;
		public double octaves = 1.0, persistence = 1.0, scale = 1.0;
		public float magnitude = 1, heightOffset = 0;

		@Override
		public float height(Vec3 pos, float height) {
			pos = new Vec3(pos).add(offset);
			return Simplex.noise3d(seed, octaves, persistence, scale, pos.x, pos.y, pos.z) * magnitude + heightOffset + height;
		}
	}
	/**
	 * A pass for clamping the height between 2 values.
	 */
	public static class ClampHeight extends HeightPass {
		public float min, max;

		public ClampHeight(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public float height(Vec3 pos, float height) {
			return Mathf.clamp(height, min, max);
		}
	}
	/**
	 * uses the angle relative to a certain direction as input to an interp function.
	 */
	public static class AngleInterpHeight extends HeightPass {
		public Vec3 dir = new Vec3();
		public Interp interp = Interp.linear;
		public float magnitude = 1;

		@Override
		public float height(Vec3 pos, float height) {
			return interp.apply(1f - pos.angle(dir)/180f) * magnitude + height;
		}
	}
}
