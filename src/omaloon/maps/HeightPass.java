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
	 * A pass that affects points inside a sphere.
	 */
	public static class SphereHeight extends HeightPass {
		/**
		 * Position of the sphere relative to the planet.
		 */
		public Vec3 pos = new Vec3();
		/**
		 * Radius of the sphere.
		 */
		public float radius = 0f;
		/**
		 * Height offset applied inside the sphere
		 */
		public float offset = 0f;
		/**
		 * When true, this pass will set the current height to the offset instead of increasing the height by the offset.
		 */
		public boolean set = false;

		@Override
		public float height(Vec3 pos, float height) {
			if (pos.dst(this.pos) < radius) return offset + height * (set ? 0f : 1f);
			return height;
		}
	}
	/**
	 * A pass that affects points based on noise.
	 * @see Simplex
	 */
	public static class NoiseHeight extends HeightPass {
		/**
		 * Offset for the noise sample relative to the planet. Values far away from the origin are reccomended.
		 */
		public Vec3 offset = new Vec3();
		/**
		 * Noise seed.
		 */
		public int seed;
		/**
		 * The amount of octves added to the noise.
		 */
		public double octaves = 1.0;
		/**
		 * Intensity multiplier for each octave.
		 */
		public double persistence = 1.0;
		/**
		 * Noise scale.
		 */
		public double scale = 1.0;
		/**
		 * Noise magnitude.
		 */
		public float magnitude = 1;
		/**
		 * Value offset applied to the noise result.
		 */
		public float heightOffset = 0;

		@Override
		public float height(Vec3 pos, float height) {
			return Simplex.noise3d(seed, octaves, persistence, scale, pos.x + offset.x, pos.y + offset.y, pos.z + offset.z) * magnitude + heightOffset + height;
		}
	}
	/**
	 * A pass that clamps the current point's height between a min and max value.
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
	 * A pass that affects points based on it's dot product in relation to a direction.
	 */
	public static class DotHeight extends HeightPass {
		/**
		 * Main direction vector. Is normalized later.
		 */
		public Vec3 dir = new Vec3();

		/**
		 * Min and max dot result where this pass applies.
		 */
		public float min = -1f;
		public float max = 1f;

		/**
		 * When true, the dot result will be mapped from min to max instead of -1 to 1.
		 */
		public boolean map = true;

		/**
		 * Interpolation curve applied to the mapped dot result.
		 */
		public Interp interp = Interp.linear;
		/**
		 * Magnitude applied to the final height offset.
		 */
		public float magnitude = 1;

		@Override
		public float height(Vec3 pos, float height) {
			float dot = dir.nor().dot(pos);
			if (dot < min || dot > max) return height;
			dot = Mathf.map(dot, map ? min : -1f, map ? max : 1f, 0f, 1f);
			return interp.apply(dot) * magnitude + height;
		}
	}
}
