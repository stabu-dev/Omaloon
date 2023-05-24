package ol.atlas;

import mindustry.world.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A block for layers, it's created to get them and then load them
 */
public interface ILayerBlock {
    /**
     * Gets a list of layers, value must be NotNull
     * @return a list of layers
     */
    @NotNull List<ILayer> getLayers();

    /**
     * Loads block layers, usually layers so load sprites
     * @param block block
     */
    default void loadLayers(Block block) {
        getLayers().forEach((layer) -> {
            if(layer != null) {
                layer.load(block);
            }
        });
    }
}