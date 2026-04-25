package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class CraterMesh extends HexMesh{
    public final CraterData craterData;
    public final HeightFunc heightFunc;
    public final ColorFunc colorFunc;
    public final Color groundColor;
    public final Color glaciumColor;

    public CraterMesh(Planet planet, int divisions, float radius, CraterData craterData, HeightFunc heightFunc, ColorFunc colorFunc, Color groundColor, Color glaciumColor){
        this.planet = planet;
        this.craterData = craterData;
        this.heightFunc = heightFunc;
        this.colorFunc = colorFunc;
        this.groundColor = groundColor;
        this.glaciumColor = glaciumColor;
        this.shader = Shaders.planet;

        this.mesh = MeshBuilder.buildHex(new HexMesher(){
            @Override
            public float getHeight(Vec3 pos){
                Vec3 deformed = craterData.getDeformedPosition(pos);
                float baseHeight = heightFunc.get(deformed);
                return craterData.processHeight(pos, baseHeight, radius == 1f);
            }

            @Override
            public void getColor(Vec3 pos, Color out){
                Vec3 deformed = craterData.getDeformedPosition(pos);
                float baseHeight = heightFunc.get(deformed);
                float h = craterData.processHeight(pos, baseHeight, radius == 1f);
                colorFunc.get(deformed, h, out);

                float glacium = craterData.getGlaciumFactor(pos);
                if(glacium > 0.05f){
                    float sGlacium = glacium * glacium * (3f - 2f * glacium);
                    out.lerp(glaciumColor, sGlacium * 0.8f);
                }

                float pierce = craterData.getPierceFactor(pos);
                if(pierce > 0.35f){
                    out.set(groundColor);
                }
            }
        }, divisions, radius, 0.2f);
    }

    public interface HeightFunc{
        float get(Vec3 pos);
    }

    public interface ColorFunc{
        void get(Vec3 pos, float height, Color out);
    }
}