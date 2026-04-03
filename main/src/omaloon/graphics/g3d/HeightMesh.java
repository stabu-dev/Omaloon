package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;


public class HeightMesh extends HexMesh{
    public HeightMesh(Planet planet, int seed, int divisions, float radius, int octaves, float persistence, float scale, float mag, HeightColorFunc heightMap){
        this.planet = planet;
        this.shader = Shaders.planet;
        this.mesh = MeshBuilder.buildHex(new HexMesher() {
            public float getHeight(Vec3 position) {
                return Simplex.noise3d(7 + seed, octaves, persistence, scale, 5f + position.x, 5f + position.y, 5f + position.z) * mag;
            }

            public void getColor(Vec3 position, arc.graphics.Color out) {
                float height = getHeight(position);
                out.set(heightMap.get(height));
            }
        }, divisions, radius, 0.2f);
    }

    public interface HeightColorFunc {
        Color get(float f);
    }
}
