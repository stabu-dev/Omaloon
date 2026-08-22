package omaloon.world.patterns.shape;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import omaloon.struct.*;
import omaloon.utils.*;

/**
 * A custom shape defined by reading black pixels from an atlas texture mask.
 * Automatically calculates the optimal anchor point closest to the geometric center.
 * @author stabu_
 */
public class CustomShape extends Shape{
    public final String maskName;
    private int width = 1, height = 1;
    private static final int colorBlack = 255, empty = 1, part = 2;
    private BitWordList blocks;
    private boolean built = false;

    public CustomShape(String maskName){
        this.maskName = maskName;
        this.blocks = new BitWordList(1, BitWordList.WordLength.two);
        this.blocks.set(0, (byte)empty);
    }

    @Override
    public void load(){
        if(built) return;

        PixmapRegion pixmap = Core.atlas.getPixmap(Core.atlas.find(this.maskName));

        if(pixmap == null){
            Log.err("Pixmap for CustomShape is null for mask: @", maskName);
            return;
        }

        this.width = pixmap.width;
        this.height = pixmap.height;
        this.blocks = new BitWordList(width * height, BitWordList.WordLength.two);

        OlUtils.readTexturePixels(pixmap, (color, index) -> {
            blocks.set(index, (byte)(color == colorBlack ? part : empty));
        });

        int bestDist = Integer.MAX_VALUE;
        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                if(blocks.get(i + j * width) != part) continue;
                int dx = i * 2 - (width - 1);
                int dy = j * 2 - (height - 1);
                int dist = dx * dx + dy * dy;
                if(dist < bestDist || (dist == bestDist && (i < anchorX || (i == anchorX && j < anchorY)))){
                    bestDist = dist;
                    anchorX = i;
                    anchorY = j;
                }
            }
        }
        this.built = true;
    }

    @Override
    public int width(){
        return width;
    }

    @Override
    public int height(){
        return height;
    }

    @Override
    public boolean get(int x, int y){
        if(x < 0 || x >= width || y < 0 || y >= height) return false;
        return blocks.get(x + y * width) == part;
    }
}
