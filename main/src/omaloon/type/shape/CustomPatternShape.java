package omaloon.type.shape;

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
        // Initialize with a default empty shape to avoid errors if build is not called
        this.blocks = new BitWordList(1, BitWordList.WordLength.two);
        this.blocks.set(0, (byte)1);
    }

    public void buildFromPixmap(PixmapRegion pixmap){
        if(built){
            return; // Already built
        }

        if(pixmap == null){
            Log.err("Pixmap for CustomPatternShape is null for mask: @", maskName);
            return;
        }

        this.width = pixmap.width;
        this.height = pixmap.height;
        this.blocks = new BitWordList(width * height, BitWordList.WordLength.two);

        OlUtils.readTexturePixels(pixmap, (color, index) -> {
            // from CustomShapeProp
            // 2815 = blue, center
            // 255 = black, part of shape
            // other = transparent, not part of shape
            switch(color){
                case 2815:
                    blocks.set(index, (byte)3);
                    break; // Center
                case 255:
                    blocks.set(index, (byte)2);
                    break;  // Part of shape
                default:
                    blocks.set(index, (byte)1);
                    break;   // Not part of shape
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
        if(x < 0 || x >= width || y < 0 || y >= height){
            return false;
        }
        byte id = blocks.get(x + y * width);
        return id == 2 || id == 3;
    }

    @Override
    public void each(Intc2 consumer){
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            byte id = blocks.get(i);
            if(id == 2 || id == 3){ // Part of shape or center
                consumer.get(i % width, i / width);
            }
        }
    }
}


