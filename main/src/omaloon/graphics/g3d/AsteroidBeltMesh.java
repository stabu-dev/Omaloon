package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * A multi-mesh that renders a toroidal asteroid belt, matching vanilla asteroid visuals.
 */
public class AsteroidBeltMesh extends MultiMesh{

    public AsteroidBeltMesh(Planet planet, int count,
                            float beltRadius, float beltWidth, float beltHeight,
                            float minSize, float maxSize,
                            int seed, Block base, Block tint, float tintThresh){
        super(generateMeshes(planet, count, beltRadius, beltWidth, beltHeight, minSize, maxSize, seed, base, tint, tintThresh));
    }

    private static GenericMesh[] generateMeshes(Planet planet, int count,
                                                float beltRadius, float beltWidth, float beltHeight,
                                                float minSize, float maxSize,
                                                int seed, Block base, Block tint, float tintThresh){
        Rand rand = new Rand(seed);
        Seq<GenericMesh> meshesSeq = new Seq<>();

        Color color = base.mapColor;
        Color color2 = tint.mapColor.cpy().a(1f - tint.mapColor.a);

        for(int r = 0; r < count; r++){
            float angleStep = 360f / count;
            float angle = (r * angleStep) + rand.range(angleStep / 1.5f);
            float dist = beltRadius + rand.range(beltWidth / 2f);
            float yOff = rand.range(beltHeight / 2f);
            float size = rand.random(minSize, maxSize);

            float cx = Mathf.cosDeg(angle) * dist, cy = yOff, cz = Mathf.sinDeg(angle) * dist;

            int rockSeed = seed + r * 10;

            Mat3D mat = new Mat3D().setToTranslation(cx, cy, cz);

            Vec3 lightDirBeltSpace = new Vec3(-cx, -cy, -cz).nor();

            float scale = size / 0.12f;

            meshesSeq.add(new MatMesh(
            new NoiseMesh(planet, rockSeed, 2, size, 2, 0.55f, 0.45f, 14f,
            color, color2, 3, 0.6f, 0.38f, tintThresh){
                @Override
                public void preRender(PlanetParams params){
                    super.preRender(params);
                    Shaders.planet.lightDir.set(lightDirBeltSpace);
                }
            },
            mat
            ));

            int pieces = 7;
            for(int j = 0; j < pieces; j++){
                float pAngle = rand.random(360f);
                float pDist = beltRadius + rand.range(beltWidth / 2f);
                float pYOff = rand.range(beltHeight / 2f);
                float pcx = Mathf.cosDeg(pAngle) * pDist, pcy = pYOff, pcz = Mathf.sinDeg(pAngle) * pDist;
                Mat3D pMat = new Mat3D().setToTranslation(pcx, pcy, pcz);
                Vec3 pLightDir = new Vec3(-pcx, -pcy, -pcz).nor();

                meshesSeq.add(new MatMesh(
                new NoiseMesh(planet, rockSeed + j + 1, 1, (0.022f + rand.random(0.039f)) * scale, 2, 0.6f, 0.38f, 20f,
                color, color2, 3, 0.6f, 0.38f, tintThresh){
                    @Override
                    public void preRender(PlanetParams params){
                        super.preRender(params);
                        Shaders.planet.lightDir.set(pLightDir);
                    }
                },
                pMat
                ));
            }
        }

        return meshesSeq.toArray(GenericMesh.class);
    }
}
