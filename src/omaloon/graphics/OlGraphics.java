package omaloon.graphics;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;

public class OlGraphics{
    /**
     * Original code from Project Unity
     * Author: @Xeloboyo
     * Creates connections for blocks
     */
    public static TextureRegion[] getRegions(TextureRegion region, int w, int h, int tilesize){
        int size = w * h;
        TextureRegion[] regions = new TextureRegion[size];

        float tileW = (region.u2 - region.u) / w;
        float tileH = (region.v2 - region.v) / h;

        for(int i = 0; i < size; i++){
            float tileX = ((float)(i % w)) / w;
            float tileY = ((float)(i / w)) / h;
            TextureRegion reg = new TextureRegion(region);

            //start coordinate
            reg.u = Mathf.map(tileX, 0f, 1f, reg.u, reg.u2) + tileW * 0.01f;
            reg.v = Mathf.map(tileY, 0f, 1f, reg.v, reg.v2) + tileH * 0.01f;
            //end coordinate
            reg.u2 = reg.u + tileW * 0.98f;
            reg.v2 = reg.v + tileH * 0.98f;

            reg.width = reg.height = tilesize;

            regions[i] = reg;
        }
        return regions;
    }

    static int[][] joinschkdirs = {
        {-1, 1}, {0, 1}, {1, 1},
        {-1, 0},/*{X}*/{1, 0},
        {-1, -1}, {0, -1}, {1, -1},
    };

    static int[] joinsMap = {
        39, 39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
        36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 39,
        39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
        36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 3,
        3, 15, 15, 3, 3, 15, 15, 5, 5, 29, 31, 5, 5, 29, 31, 4,
        4, 40, 40, 4, 4, 20, 20, 28, 28, 10, 11, 28, 28, 23, 32, 3,
        3, 15, 15, 3, 3, 15, 15, 2, 2, 9, 14, 2, 2, 9, 14, 4,
        4, 40, 40, 4, 4, 20, 20, 30, 30, 47, 44, 30, 30, 22, 6, 39,
        39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
        36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 39,
        39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
        36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 3,
        3, 15, 15, 3, 3, 15, 15, 5, 5, 29, 31, 5, 5, 29, 31, 0,
        0, 42, 42, 0, 0, 12, 12, 8, 8, 35, 34, 8, 8, 33, 7, 3,
        3, 15, 15, 3, 3, 15, 15, 2, 2, 9, 14, 2, 2, 9, 14, 0,
        0, 42, 42, 0, 0, 12, 12, 1, 1, 45, 18, 1, 1, 19, 13
    };

    public static <T> int getMaskIndex(T[][] map, int x, int y, Boolf<T> canConnect){
        int index = 0, ax, ay;
        T t;

        for(int i = 0; i < joinschkdirs.length; i++){
            ax = joinschkdirs[i][0] + x;
            ay = joinschkdirs[i][1] + y;
            t = null;

            if(ax >= 0 && ay >= 0 && ax < map.length && ay < map[0].length){
                t = map[ax][ay];
            }

            index += canConnect.get(t) ? (1 << i) : 0;
        }

        return index;
    }

    public static <T> int getTilingIndex(T[][] map, int x, int y, Boolf<T> canConnect){
        return joinsMap[getMaskIndex(map, x, y, canConnect)];
    }

    /*end*/
    public static Rand rand = new Rand();

    public static void bubbles(int seed, float x, float y, int bubblesAmount, float bubblesSize, float baseLife, float baseSize){
        rand.setSeed(seed);

        for(int i = 0; i < bubblesAmount; i++){
            float
                angle = rand.random(360f),
                fin = (rand.random(0.8f) * (Time.time / baseLife)) % rand.random(0.1f, 0.6f),
                len = rand.random(baseSize / 2f, baseSize) / fin,

                trnsx = x + Angles.trnsx(angle, len, rand.random(baseSize / 4f, baseSize / 4f)),
                trnsy = y + Angles.trnsy(angle, len, rand.random(baseSize / 4f, baseSize / 4f));

            Fill.poly(trnsx, trnsy, 18, Interp.sine.apply(fin * 3.5f) * bubblesSize);
        }
    }

    public static void l(float layer){
        Draw.z(layer);
    }
}