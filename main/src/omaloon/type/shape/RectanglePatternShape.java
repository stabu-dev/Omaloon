package omaloon.type.shape;

import arc.func.*;

public class RectanglePatternShape extends Shape{
    public int width = 3;
    public int height = 3;

    public RectanglePatternShape(int width, int height){
        this.width = width;
        this.height = height;
    }

    public RectanglePatternShape(){
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
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public void each(Intc2 consumer){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                consumer.get(x, y);
            }
        }
    }

    @Override
    public void load(){
        // nothing to do
    }
}
