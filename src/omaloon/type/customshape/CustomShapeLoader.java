package omaloon.type.customshape;

import omaloon.struct.*;

/**
 * Original code from MindustryModCore
 * <a href="https://github.com/Zelaux/MindustryModCore">...</a>
 * by Zelaux
 */
public abstract class CustomShapeLoader<T>{
    public int width;
    public int height;
    public BitWordList blocks;

    public abstract void load(T type);

    public CustomShape loadToShape(T type){
        load(type);
        return toShape();
    }

    public CustomShape toShape(){
        return new CustomShape(width, height, blocks);
    }
}
