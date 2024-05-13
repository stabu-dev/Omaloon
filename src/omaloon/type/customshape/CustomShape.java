package omaloon.type.customshape;

import arc.func.*;
import omaloon.struct.*;

/**
 * Original code from MindustryModCore
 * <a href="https://github.com/Zelaux/MindustryModCore">...</a>
 * by Zelaux
 */
public class CustomShape{
    public final int width;
    public final int height;
    public final int nonNothingAmount;
    public final int otherBlocksAmount;
    BitWordList blocks;
    int centerX, centerY;

    public CustomShape(int width, int height, int[] blocks){
        this(width, height, mapArrayToList(width, height, blocks));
    }

    public CustomShape(int width, int height, BitWordList blocks){
        this.blocks = blocks;
        this.width = width;
        this.height = height;
        findCenter();
        int nonNothingAmount = 0;
        int otherBlocksAmount = 0;
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            BlockType block = BlockType.all[blocks.get(i)];
            if(block.isSimpleBlock()){
                nonNothingAmount++;
                otherBlocksAmount++;
            }
            if(block.isCenterBlock()){
                nonNothingAmount++;
            }
        }
        this.nonNothingAmount = nonNothingAmount;
        this.otherBlocksAmount = otherBlocksAmount;
    }

    private static BitWordList mapArrayToList(int width, int height, int[] blocks){
        BitWordList list = new BitWordList(width * height, BitWordList.WordLength.two);
        for(int i = 0; i < blocks.length && i < list.initialWordsAmount; i++){
            list.set(i, (byte)blocks[i]);
        }
        return list;
    }

    public int anchorX(){
        return centerX;
    }

    public int anchorY(){
        return centerY;
    }

    private void findCenter(){

        int ordinal = BlockType.anchorBlock.ordinal();
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            if(blocks.get(i) == ordinal){
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

    public BlockType get(int x, int y){
        return BlockType.all[blocks.get(x + y * width)];
    }

    public int getId(int x, int y){
        return blocks.get(x + y * width);
    }

    public void eachRelativeCenter(Intc2 consumer){
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            consumer.get(unpackX(i) - centerX, unpackY(i) - centerY);
        }
    }

    public void eachRelativeCenter(boolean includeNothing, boolean includeOther, boolean includeCenter, Intc2 consumer){
        for(int i = 0; i < blocks.initialWordsAmount; i++){
            BlockType type = BlockType.all[blocks.get(i)];
            if(type.isVoid() && includeNothing || type.isSimpleBlock() && includeOther || type.isCenterBlock() && includeCenter){
                consumer.get(unpackX(i) - centerX, unpackY(i) - centerY);
            }
        }
    }

    public BlockType getRelativeCenter(int x, int y){
        return get(x + centerX, y + centerY);
    }

    public int getIdRelativeCenter(int x, int y){
        return getId(x + centerX, y + centerY);
    }

    public enum BlockType{
        block(true, false), anchorBlock(true, true),
        voidBlock(false, false);
        public static final BlockType[] all = values();
        public final boolean solid;
        public final boolean center;

        BlockType(boolean solid, boolean center){
            this.solid = solid;
            this.center = center;
        }

        public boolean isVoid(){
            return !solid;
        }

        public boolean isSimpleBlock(){
            return this == block;
        }

        public boolean isCenterBlock(){
            return this == anchorBlock;
        }

        public boolean isVoidBlock(){
            return this == voidBlock;
        }
    }
}
