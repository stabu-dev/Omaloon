package omaloon.math;

import mindustry.*;

import static arc.Core.camera;
import static arc.math.Mathf.dst;

public class Math3D{
    /**
     * @author MEEP, modified by RandomJelly
     * From prog-mat-java
     * https://github.com/MEEPofFaith/prog-mats-java
     */

    public static final float horiToVerti = 1f / 48f;

    public static float xOffset(float x, float height){
        return (x - camera.position.x) * hMul(height);
    }

    public static float yOffset(float y, float height){
        return (y - camera.position.y) * hMul(height);
    }

    public static float hMul(float height){
        return height(height) * Vars.renderer.getDisplayScale();
    }

    public static float height(float height){
        return height * horiToVerti;
    }

    public static float layerOffset(float x, float y){
        float max = Math.max(camera.width, camera.height);
        return -dst(x, y, camera.position.x, camera.position.y) / max / 1000f;
    }
}
