package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class CraterPoolMesh extends HexMesh{
    public CraterPoolMesh(Planet planet, int divisions, float radius, CraterData craterData, CraterMesh.HeightFunc terrainHeightFunc, Color glaciumColor){
        this.planet = planet;
        this.shader = Shaders.planet;

        this.mesh = MeshBuilder.buildHex(new HexMesher(){
            @Override
            public float getHeight(Vec3 pos){
                float terrainH = terrainHeightFunc.get(pos);
                float poolH = craterData.getPoolHeight(pos, terrainHeightFunc);
                if(poolH < 0f) return terrainH - 0.05f;
                return poolH;
            }

            @Override
            public void getColor(Vec3 pos, Color out){
                out.set(glaciumColor);
            }

            @Override
            public boolean skip(Vec3 pos){
                return !craterData.isInLake(pos);
            }
        }, divisions, 1f, 0.2f);
    }
}