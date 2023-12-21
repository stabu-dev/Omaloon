package omaloon.utils;

import arc.graphics.g2d.*;
import arc.struct.*;
import org.jetbrains.annotations.Contract;

import static arc.Core.*;

public class OlUtils {
    @Contract(pure = true)
    public static int reverse(int rotation) {
        return switch(rotation) {
            case 0 -> 2; case 2 -> 0;case 1 -> 3; case 3 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };
    }

    public static int getByIndex(IntSet intSet, int index) {
        if (index < 0 || index >= intSet.size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] value = {0};
        final int[] counter = {0};
        intSet.each((item) -> {
            if (counter[0] == index) {
                value[0] = item;
            }
            counter[0]++;
        });

        if (counter[0] > index) {
            return value[0];
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**bittiler stuff
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
}
