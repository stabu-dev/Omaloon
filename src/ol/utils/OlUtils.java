package ol.utils;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.IntSet;

public class OlUtils {
    public static float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
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
        TextureRegion tex = Core.atlas.find(name);
        int margin = 0;
        int countX = tex.width / size;
        TextureRegion[] tiles = new TextureRegion[countX];

        for(int step = 0; step < countX; step++){
            tiles[step] = new TextureRegion(tex, step * (margin + size), layer * (margin + size), size, size);
        }
        return tiles;
    }
}
