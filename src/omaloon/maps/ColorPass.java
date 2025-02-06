package omaloon.maps;


import arc.graphics.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;

public abstract class ColorPass{
    public abstract @Nullable Color color(Vec3 pos, float height);

    /**
     * A pass that paints points inside a sphere.
     * @see HeightPass.SphereHeight SphereHeight
     */
    public static class SphereColorPass extends ColorPass{
        /**
         * Position of the sphere relative to the planet.
         */
        public Vec3 pos;
        /**
         * Radius of the sphere.
         */
        public float radius;
        /**
         * Color painted inside the sphere.
         */
        public Color out;

        public SphereColorPass(Vec3 pos, float radius, Color out){
            this.pos = pos;
            this.radius = radius;
            this.out = out;
        }

        @Override
        public Color color(Vec3 pos, float height){
            if(pos.dst(this.pos) < radius) return out;
            return null;
        }
    }

    /**
     * A pass that uses noise to fill regions on the planet.
     * @see HeightPass.NoiseHeight NoiseHeight
     */
    public static class NoiseColorPass extends ColorPass{
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
         * Min and max treshold values of noise that paint will apply
         */
        public float min = 0f, max = 1f;
        /**
         * Color painted based on the noise.
         */
        public Color out = Color.white;

        @Override
        public Color color(Vec3 pos, float height){
            float noise = Simplex.noise3d(seed, octaves, persistence, scale, pos.x + offset.x, pos.y + offset.y, pos.z + offset.z) * magnitude;
            if(min <= noise && noise <= max) return out;
            return null;
        }
    }

    /**
     * A pass that paints regions whose height is within a boundary.
     * @see HeightPass.ClampHeight ClampHeight
     */
    public static class FlatColorPass extends ColorPass{
        public float min = 0f, max = 1f;
        public Color out = Color.white;

        @Override
        public Color color(Vec3 pos, float height){
            if(min <= height && height <= max) return out;
            return null;
        }
    }
}