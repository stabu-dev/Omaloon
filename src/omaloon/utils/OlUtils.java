package omaloon.utils;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.type.*;
import omaloon.ui.*;
import org.jetbrains.annotations.*;

import static arc.Core.atlas;

public class OlUtils{
    public static float angleDistSigned(float a, float b){
        a += 360f;
        a %= 360f;
        b += 360f;
        b %= 360f;
        float d = Math.abs(a - b) % 360f;
        int sign = (a - b >= 0f && a - b <= 180f) || (a - b <= -180f && a - b >= -360f) ? 1 : -1;
        return (d > 180f ? 360f - d : d) * sign;
    }

    public static float angleDistSigned(float a, float b, float start){
        float dst = angleDistSigned(a, b);
        if(Math.abs(dst) > start){
            return dst > 0 ? dst - start : dst + start;
        }
        return 0f;
    }

    public static float angleDist(float a, float b){
        float d = Math.abs(a - b) % 360f;
        return (d > 180f ? 360f - d : d);
    }

    public static void shotgun(int points, float spacing, float offset, Floatc cons){
        for(int i = 0; i < points; i++){
            cons.get(i * spacing - (points - 1) * spacing / 2f + offset);
        }
    }

    public static float clampedAngle(float angle, float relative, float limit){
        if(limit >= 180) return angle;
        if(limit <= 0) return relative;
        float dst = angleDistSigned(angle, relative);
        if(Math.abs(dst) > limit){
            float val = dst > 0 ? dst - limit : dst + limit;
            return (angle - val) % 360f;
        }
        return angle;
    }

    @Contract(pure = true)
    public static int reverse(int rotation){
        return switch(rotation){
            case 0 -> 2;
            case 2 -> 0;
            case 1 -> 3;
            case 3 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };
    }

    public static Item getByIndexAsItem(MultiItemData data, int index){
        if(index < 0 || index >= data.length()){
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for MultiItemData of size " + data.length());
        }

        return data.getItem(index);
    }

    public static int getByIndex(IntSet intSet, int index){
        if(index < 0 || index >= intSet.size){
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for IntSet of size " + intSet.size);
        }

        int counter = 0;
        for(IntSet.IntSetIterator iterator = intSet.iterator(); iterator.hasNext; ){
            int item = iterator.next();
            if(counter == index){
                return item;
            }
            counter++;
        }

        throw new IllegalArgumentException("Index out of range for IntSet.");
    }

    /**
     * bittiler stuff
     * <p>
     * Original code from Serilia[<a href="https://github.com/Froomeeth/Serilia/blob/main/src/serilia/util/SeUtil.java#L64C1-L64C1">...</a>]
     */
    public static TextureRegion[][] splitLayers(String name, int size, int layerCount){
        TextureRegion[][] layers = new TextureRegion[layerCount][];

        for(int i = 0; i < layerCount; i++){
            layers[i] = split(name, size, i);
        }
        return layers;
    }

    public static TextureRegion[] split(String name, int size, int layer){
        TextureRegion tex = atlas.find(name);
        int margin = 0;
        int countX = tex.width / size;
        TextureRegion[] tiles = new TextureRegion[countX];

        for(int step = 0; step < countX; step++){
            tiles[step] = new TextureRegion(tex, step * (margin + size), layer * (margin + size), size, size);
        }
        return tiles;
    }

    /**
     * reads every single pixel on a textureRegion from bottom left to top right
     */
    public static void readTexturePixels(PixmapRegion pixmap, Intc2 cons){
        for(int j = 0; j < pixmap.height; j++){
            for(int i = 0; i < pixmap.width; i++){
                cons.get(pixmap.get(i, j), i + pixmap.width * (pixmap.height - 1 - j));
            }
        }
    }
}
