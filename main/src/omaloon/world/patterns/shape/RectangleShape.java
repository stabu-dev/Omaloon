package omaloon.world.patterns.shape;

/**
 * A standard rectangular bounding-box shape where all tiles within width and height are filled.
 * @author stabu_
 */
public class RectangleShape extends Shape{
    public int width = 3;
    public int height = 3;

    public RectangleShape(int width, int height){
        this.width = width;
        this.height = height;
    }

    public RectangleShape(){
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
    public void load(){
    }
}
