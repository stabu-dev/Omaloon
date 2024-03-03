package omaloon.maps;


import arc.graphics.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;

public abstract class ColorPass {
	public abstract @Nullable Color color(Vec3 pos, float height);

	/**
	 * A pass that fills regions inside a sphere for use in craters
	 * @see omaloon.maps.HeightPass.CraterHeight CraterHeight
	 */
	public static class CraterColorPass extends ColorPass {
		public Vec3 position;
		public float radius;
		public Color out;

		public CraterColorPass(Vec3 position, float radius, Color out) {
			this.position = position;
			this.radius = radius;
			this.out = out;
		}

		@Override
		public Color color(Vec3 pos, float height) {
			if (pos.dst(position) < radius) return out;
			return null;
		}
	}
	/**
	 * A pass that uses noise to fill regions on the planet
	 * @see omaloon.maps.HeightPass.NoiseHeight NoiseHeight
	 */
	public static class NoiseColorPass extends ColorPass {
		public int seed;
		public double octaves = 1.0, persistence = 1.0, scale = 1.0;
		public float magnitude = 1, min = 0f, max = 1f;
		public Vec3 offset = new Vec3();
		public Color out = Color.white;

		@Override
		public Color color(Vec3 pos, float height) {
			pos = new Vec3(pos).add(offset);
			float noise = Simplex.noise3d(seed, octaves, persistence, scale, pos.x, pos.y, pos.z) * magnitude;
			if (min < noise && noise < max) return out;
			return null;
		}
	}
	/**
	 * A pass that paints regions whose height is within a boundary
	 */
	public static class FlatColorPass extends ColorPass {
		public float min = 0f, max = 1f;
		public Color out = Color.white;

		@Override
		public Color color(Vec3 pos, float height) {
			if (min < height && height < max) return out;
			return null;
		}
	}
}