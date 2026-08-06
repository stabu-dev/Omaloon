package omaloon.type.shape;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.util.*;
import omaloon.struct.*;
import omaloon.utils.*;

public class CustomPatternShape extends Shape{

    public final String maskName;
    private int width = 1, height = 1;
    private static final int colorBlack = 255, empty = 1, part = 2;
    private BitWordList blocks;
    private boolean built = false;

    public CustomPatternShape(String maskName){
        this.maskName = maskName;
        this.blocks = new BitWordList(1, BitWordList.WordLength.two);
        this.blocks.set(0, (byte)empty);
    }

    @Override
    public void load(){
        if(built) return;

        PixmapRegion pixmap = Core.atlas.getPixmap(Core.atlas.find(this.maskName));

        if(pixmap == null){
            Log.err("Pixmap for CustomPatternShape is null for mask: @", maskName);
            return;
        }

        this.width = pixmap.width;
        this.height = pixmap.height;
        this.blocks = new BitWordList(width * height, BitWordList.WordLength.two);

        OlUtils.readTexturePixels(pixmap, (color, index) -> {
            int x = index % width;
            int yPix = index / width;
            int yWorld = (height - 1) - yPix;
            int newIndex = x + yWorld * width;
            blocks.set(newIndex, (byte)(color == colorBlack ? part : empty));
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
        int mx = x + anchorX;
        int my = y + anchorY;
        if(mx < 0 || mx >= width || my < 0 || my >= height) return false;
        return blocks.get(mx + my * width) == part;
    }

    @Override
    public void each(Intc2 consumer){
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(get(x - anchorX, y - anchorY)) consumer.get(x - anchorX, y - anchorY);
            }
        }
    }
}