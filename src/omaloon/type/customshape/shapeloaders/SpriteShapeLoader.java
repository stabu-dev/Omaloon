package omaloon.type.customshape.shapeloaders;

import arc.graphics.*;
import omaloon.type.customshape.*;

/**
 * Original code from MindustryModCore
 * <a href="https://github.com/Zelaux/MindustryModCore">...</a>
 * by Zelaux
 */
public class SpriteShapeLoader extends CustomShapeLoader<Pixmap>{
    /*
    public final int chunkSize;
    public final ChunkProcessor chunkProcessor;

    public SpriteShapeLoader(int chunkSize, ChunkProcessor chunkProcessor){
        this.chunkSize = chunkSize;
        this.chunkProcessor = chunkProcessor;
    }


     */
    @Override
    public void load(Pixmap type){
			/*
        width = type.width / chunkSize;
        height = type.height / chunkSize;
        blocks = new BitWordList(width * height, WordLength.two);
        for(int chunkX = 0; chunkX < width; chunkX++){
            for(int chunkY = 0; chunkY < height; chunkY++){
                int index = chunkX + (height-1-chunkY) * width;
                blocks.set(index, (byte)chunkProcessor.process(type, chunkX, chunkY, chunkSize).ordinal());
            }
        }

			 */
    }/*

    public interface ChunkProcessor{
        BlockType process(Pixmap pixmap, int chunkX, int chunkY, int size);

        class PercentProcessor implements ChunkProcessor{
            /** [0-1]
            public float percent;
            public int anchorChunkX;
            public int anchorChunkY;

            public PercentProcessor(float percent, int anchorChunkX, int anchorChunkY){
                this.percent = percent;
                this.anchorChunkX = anchorChunkX;
                this.anchorChunkY = anchorChunkY;
            }

            @Override
            public BlockType process(Pixmap pixmap, int chunkX, int chunkY, int size){
                if(chunkX == anchorChunkX && chunkY == anchorChunkY) return BlockType.anchorBlock;
                int total = size * size;
                int worldX = chunkX * size;
                int worldY = chunkY * size;
                float counter = 0;
                for(int dx = 0; dx < size; dx++){
                    for(int dy = 0; dy < size; dy++){
                        counter += pixmap.getA(worldX + dx, worldY + dy) / 255f;
                    }
                }
                return counter / total > percent ? BlockType.block : BlockType.voidBlock;
            }
        }
    }
    */
}
