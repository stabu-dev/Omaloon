package omaloon.type.customshape;

import arc.func.*;
import omaloon.struct.*;

/**
 * Original code from MindustryModCore
 * <a href="https://github.com/Zelaux/MindustryModCore">...</a>
 * modified for simplicity.
 * @author Zelaux
 */
public class CustomShape{
    public final int width;
    public final int height;
    public BitWordList blocks;
    int centerX, centerY;

    public CustomShape(int width, int height, BitWordList blocks){
        this.blocks = blocks;
        this.width = width;
        this.height = height;
        findCenter();
    }

    public int centerX(){
        return centerX;
    }

    public int centerY(){
        return centerY;
    }

    public void findCenter(){
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            if(blocks.get(i) == 3){
                centerX = unpackX(i);
                centerY = unpackY(i);
                return;
            }
        }
        throw new RuntimeException("Cannot find center");
    }

    public int unpackX(int index){
        return index % width;
    }

    public int unpackY(int index){
        return index / width;
    }

    public int getId(int x, int y){
        return blocks.get(x + y * width);
    }

    public void eachRelativeCenter(Intc2 consumer){
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            consumer.get(unpackX(i) - centerX, unpackY(i) - centerY);
        }
    }

    public int getIdRelativeCenter(int x, int y){
        return getId(x + centerX, y + centerY);
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof CustomShape shape)) return false;
        return width == shape.width && height == shape.height && blocks.equals(shape.blocks);
    }
}
