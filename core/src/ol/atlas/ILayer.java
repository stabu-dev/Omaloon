package ol.atlas;

import mindustry.gen.Building;
import mindustry.world.Block;

/**
 * The layer used to draw the block
 */
public interface ILayer {
    /**
     * Draws, the method is clear
     * @param block building block
     * @param build the building for which the layer is drawing
     */
    void draw(Block block, Building build);

    /**
     * Loads a layer, usually sprites or block validation
     * @param block the block for which the layer loads
     */
    void load(Block block);
}