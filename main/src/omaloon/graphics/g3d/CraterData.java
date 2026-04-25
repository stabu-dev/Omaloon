package omaloon.graphics.g3d;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;

public class CraterData{
    public final Crater[] craters;
    public final int seed;

    public float noiseIntensity = 0.16f;
    public int noiseOctaves = 3;
    public double noisePersistence = 0.5;
    public double noiseScale = 8.0;

    public CraterData(int seed, int count, CraterMesh.HeightFunc baseHeightFunc){
        this.seed = seed;
        Rand rand = new Rand(seed);
        craters = new Crater[count];

        Vec3 ringNormal = new Vec3(Vec3.Y).rotate(Vec3.X, 22f);
        Vec3 u = ringNormal.cpy().crs(ringNormal.equals(Vec3.Y) ? Vec3.X : Vec3.Y).nor();
        Vec3 v = ringNormal.cpy().crs(u).nor();

        for(int i = 0; i < count; i++){
            Crater c = new Crater();
            c.id = i;

            Vec3 bestPos = null;
            float maxMinDist = -1f;
            for(int attempt = 0; attempt < 8; attempt++){
                float theta = rand.random(Mathf.PI2);
                float bandWidth = rand.random(-0.25f, 0.25f);
                Vec3 testPos = new Vec3().add(u.cpy().scl((float)Math.cos(theta))).add(v.cpy().scl((float)Math.sin(theta))).add(ringNormal.cpy().scl(bandWidth)).nor();

                float minDist = 100f;
                for(int j = 0; j < i; j++) minDist = Math.min(minDist, angDist(testPos, craters[j].position));
                if(minDist > maxMinDist){
                    maxMinDist = minDist;
                    bestPos = testPos;
                }
            }
            c.position = bestPos;
            c.centerBaseHeight = baseHeightFunc.get(c.position);

            float sizeRoll = rand.random(1f);
            c.radius = 0.03f + sizeRoll * sizeRoll * 0.18f;

            c.depth = c.radius * rand.random(0.7f, 2.2f);
            c.rimHeight = c.depth * rand.random(0.3f, 1.1f);

            float typeRoll = rand.random(1f);
            if(typeRoll < 0.40f) c.type = CraterType.simple;
            else if(typeRoll < 0.70f){
                c.type = CraterType.complex;
                c.peakHeight = c.depth * rand.random(0.8f, 1.8f);
                c.peakRadius = c.radius * rand.random(0.15f, 0.45f);
            }else{
                c.type = CraterType.elliptical;
                c.eccentricity = rand.random(0.3f, 1.2f);
                c.ellipseAxis = new Vec3(rand.range(1f), rand.range(1f), rand.range(1f)).nor();
            }

            c.noiseSeed = seed + i * 19 + 7;
            c.boundingDot = (float)Math.cos(c.radius * 3.5f);
            craters[i] = c;
        }
    }

    private float getEffectiveDist(Crater c, Vec3 pos){
        float d = angDist(pos, c.position);
        float noise = 1f + getRimNoise(c, pos) * 0.5f;
        if(c.type == CraterType.elliptical){
            if(d < 0.0001f) return 0f;
            float axisDot = Math.abs(new Vec3(pos).sub(c.position).nor().dot(c.ellipseAxis));
            d /= (1f + c.eccentricity * axisDot);
        }
        return d / (c.radius * noise);
    }

    public boolean hasLake(Crater c){
        return c.centerBaseHeight >= 0.012f && (c.id % 4 != 0);
    }

    public float processHeight(Vec3 pos, float baseHeight, boolean isIceMesh){
        Vec3 defPos = getDeformedPosition(pos);
        float currentHeight = baseHeight;
        float maxRim = 0f;

        for(Crater c : craters){
            if(defPos.dot(c.position) < c.boundingDot) continue;
            float normDist = getEffectiveDist(c, defPos);

            boolean inOcean = c.centerBaseHeight < 0.012f;
            float rimRange = inOcean ? (0.6f + c.radius * 6f) : (1f + c.radius * 4f);
            if(normDist > rimRange) continue;

            float depth = c.depth;
            float rimH = c.rimHeight;
            float peakH = c.peakHeight;

            if(inOcean){
                depth *= 0.1f;
                rimH *= 1.4f;
                peakH *= 1.4f;
            }

            if(normDist < 1f){
                float noise = Simplex.noise3d(c.noiseSeed + 2, 3, 0.5, 5.0, defPos.x, defPos.y, defPos.z) * 0.02f;
                float floorHeight = c.centerBaseHeight - depth * (1f - normDist * normDist) + noise;

                if(c.type == CraterType.complex && normDist < c.peakRadius / c.radius){
                    float p = normDist / (c.peakRadius / c.radius);
                    floorHeight += peakH * (1f - p * p);
                }

                if(isIceMesh && c.centerBaseHeight >= 0.012f){
                    floorHeight -= 0.2f;
                }

                float weight = 1f - normDist;
                float interp = weight * weight * (3f - 2f * weight);
                currentHeight = Mathf.lerp(currentHeight, Math.min(currentHeight, floorHeight), interp);
            }else{
                float r = (normDist - 1f) / (c.radius * 6f);
                float rimWeight = (float)Math.exp(-r * r * 3.5f);
                maxRim = Math.max(maxRim, rimH * rimWeight);
            }
        }

        return currentHeight + maxRim;
    }

    public Vec3 getDeformedPosition(Vec3 pos){
        Vec3 deformed = new Vec3(pos);
        Vec3 totalPush = new Vec3();
        for(Crater c : craters){
            if(pos.dot(c.position) < c.boundingDot) continue;
            float dist = angDist(pos, c.position);
            boolean inOcean = c.centerBaseHeight < 0.012f;
            float deformRange = inOcean ? c.radius * 1.3f : c.radius * 2.2f;
            if(dist > deformRange) continue;
            float normDist = dist / c.radius;

            float radiusScale = Math.min(0.06f, c.radius) / c.radius;
            float t = (normDist < 1f ? normDist : Math.max(0f, 1f - (normDist - 1f) / 1.2f));
            t = t * t * (3f - 2f * t);
            float pushAmt = t * 0.35f * radiusScale;

            float jitter = Simplex.noise3d(c.noiseSeed + 1, 2, 0.5, 10.0, pos.x, pos.y, pos.z) * 0.08f;
            totalPush.add(new Vec3(pos).sub(c.position).nor().scl(pushAmt * (1f + jitter) * c.radius));
        }
        return deformed.sub(totalPush).nor();
    }

    public float getGlaciumFactor(Vec3 pos){
        float glacium = 0f;
        Vec3 defPos = getDeformedPosition(pos);
        for(Crater c : craters){
            if(defPos.dot(c.position) < c.boundingDot) continue;
            float normDist = getEffectiveDist(c, defPos);
            if(normDist > 1f) continue;

            float factor = 0f;
            if(normDist <= 0.85f && c.radius >= 0.04f){
                factor = (1f - normDist / 0.85f);
                if(c.centerBaseHeight < 0.012f) factor *= 0.2f;
                if(c.type == CraterType.complex && normDist < (c.peakRadius * 0.6f) / c.radius) factor *= (normDist / ((c.peakRadius * 0.6f) / c.radius));
            }

            float weight = (1f - normDist);
            weight = weight * weight * (3f - 2f * weight);
            glacium = Math.max(glacium, factor * weight);
        }
        return Mathf.clamp(glacium, 0f, 1f);
    }

    public float getPoolHeight(Vec3 pos, CraterMesh.HeightFunc terrainHeightFunc){
        for(Crater c : craters){
            if(c.centerBaseHeight < 0.012f || c.id % 4 == 0) continue;
            float dist = angDist(getDeformedPosition(pos), c.position);
            if(dist > c.radius) continue;

            float fillFactor = 0.9f - c.radius * 5f - (c.id % 4) * 0.05f;
            fillFactor = Math.max(0.1f, Math.min(0.9f, fillFactor));
            return c.centerBaseHeight - c.depth * fillFactor;
        }
        return -1f;
    }

    public boolean isInLake(Vec3 pos){
        for(Crater c : craters){
            if(c.centerBaseHeight < 0.012f || c.id % 4 == 0) continue;
            float dist = angDist(getDeformedPosition(pos), c.position);
            if(dist <= c.radius) return true;
        }
        return false;
    }

    public float getPierceFactor(Vec3 pos){
        float max = 0f;
        Vec3 defPos = getDeformedPosition(pos);
        for(Crater c : craters){
            if(defPos.dot(c.position) < c.boundingDot) continue;
            float normDist = getEffectiveDist(c, defPos);
            if(normDist > 1.35f) continue;

            float intensity = 1.0f;
            if(c.centerBaseHeight >= 0.012f){
                intensity = hasLake(c) ? 1.0f : 0.2f;
            }

            float factor;
            if(c.centerBaseHeight < 0.012f){
                factor = (normDist < 0.65f) ? 0f : (1.1f - Math.abs(normDist - 1.05f) * 2.5f);
            }else{
                float bowlFactor = 1.1f - (normDist / 1.1f);
                float rnd = (c.id % 7) * 0.02f;
                float rimFactor = 1.25f - Math.abs(normDist - (1.0f + rnd)) * 3.0f;
                factor = Math.max(bowlFactor, rimFactor) * intensity;
            }

            factor *= Mathf.clamp(c.depth / 0.05f, 0f, 1f);
            max = Math.max(max, factor);
        }
        return Mathf.clamp(max, 0f, 1f);
    }

    private float getRimNoise(Crater c, Vec3 pos){
        return Simplex.noise3d(c.noiseSeed, noiseOctaves, noisePersistence, noiseScale, pos.x + 100, pos.y + 100, pos.z + 100) * noiseIntensity;
    }

    public float angDist(Vec3 a, Vec3 b){
        return (float)Math.acos(Mathf.clamp(a.dot(b), -1f, 1f));
    }

    public enum CraterType{simple, complex, elliptical}

    public static class Crater{
        public int id;
        public Vec3 position;
        public float radius, depth, rimHeight;
        public CraterType type;
        public float peakHeight, peakRadius;
        public float eccentricity;
        public int noiseSeed;
        public float centerBaseHeight;
        public float boundingDot;
        Vec3 ellipseAxis;
    }
}