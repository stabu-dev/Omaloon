package omaloon.maps.planets;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.maps.generators.*;

public class GlassmorePlanetGenerator extends PlanetGenerator {
	public Seq<Height> heights = new Seq<>();
	public Seq<ColorPass> colors = new Seq<>();
	public float baseHeight = 1;
	public Color baseColor = Color.white;

	public float rawHeight(Vec3 position) {
		float height = baseHeight;
		for (Height h : heights) {
			height = h.height(position, height);
		}
		return height;
	}

	@Override
	public float getHeight(Vec3 position) {
		return rawHeight(position);
	}

	@Override
	public Color getColor(Vec3 position) {
		Color color = baseColor;
		for (ColorPass c : colors) {
			if (c.color(position, rawHeight(position)) != null) color = c.color(position, rawHeight(position));
		}
		return color;
	}

	public abstract static class Height {
		abstract float height(Vec3 pos, float height);
	}
	public static class CraterHeight extends Height {
		public Vec3 position;
		public float radius, offset;

		public CraterHeight(Vec3 position, float radius, float offset) {
			this.position = position;
			this.radius = radius;
			this.offset = offset;
		}

		@Override
		public float height(Vec3 pos, float height) {
			if (pos.dst(position) < radius) return offset + height;
			return height;
		}
	}
	public static class NoiseHeight extends Height {
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
	public static class ClampHeight extends Height {
		public float min, max;

		public ClampHeight(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		float height(Vec3 pos, float height) {
			return Mathf.clamp(height, min, max);
		}
	}
	public static class AngleInterpHeight extends Height {
		public Vec3 dir = new Vec3();
		public Interp interp = Interp.linear;
		public float magnitude = 1;

		@Override
		float height(Vec3 pos, float height) {
			return interp.apply(1f - pos.angle(dir)/180f) * magnitude + height;
		}
	}

	public abstract static class ColorPass {
		abstract @Nullable Color color(Vec3 pos, float height);
	}
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
