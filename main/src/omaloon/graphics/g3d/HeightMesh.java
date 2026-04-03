package omaloon.graphics.g3d;

import arc.func.*;
import arc.graphics.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;


public class HeightMesh extends HexMesh{
    public HeightMesh(Planet planet, int divisions, float radius, Floatf<Vec3> heightFunc, HeightColorFunc heightMap){
        this.planet = planet;
        this.shader = Shaders.planet;
        this.mesh = MeshBuilder.buildHex(new HexMesher() {
            public float getHeight(Vec3 position) {
                return heightFunc.get(position);
            }

            public void getColor(Vec3 position, arc.graphics.Color out) {
                out.set(heightMap.get(position, getHeight(position)));
            }
        }, divisions, radius, 0.2f);
    }

    public interface HeightColorFunc {
        Color get(Vec3 position, float height);
    }
}
