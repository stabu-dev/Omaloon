package omaloon.type.shape;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.util.*;
import omaloon.struct.*;
import omaloon.utils.*;

public class CustomPatternShape extends Shape{
    public final String maskName;
    private int width = 1;
    private int height = 1;
    private BitWordList blocks;
    private boolean built = false;

    public CustomPatternShape(String maskName){
        this.maskName = maskName;
        this.blocks = new BitWordList(1, BitWordList.WordLength.two);
        this.blocks.set(0, (byte)1);
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
            int y_pix = index / width;
            // Convert Pixmap Y (top-down) to World Y (bottom-up)
            int y_world = (height - 1) - y_pix;
            int newIndex = x + y_world * width;

            switch(color){
                case 2815: // blue, center
                    blocks.set(newIndex, (byte)3);
                    break;
                case 255: // black, part of shape
                    blocks.set(newIndex, (byte)2);
                    break;
                default:
                    blocks.set(newIndex, (byte)1);
                    break;
            }
        });
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
        byte id = blocks.get(x + y * width);
        return id == 2 || id == 3;
    }

    @Override
    public void each(Intc2 consumer){
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(get(x, y)) consumer.get(x, y);
            }
        }
    }
}