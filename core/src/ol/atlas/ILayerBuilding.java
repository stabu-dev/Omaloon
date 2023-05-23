package ol.atlas;

import mindustry.gen.Building;
import mindustry.world.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Інтерфейс для будівлі, яка буде малювати шари
 */
public interface ILayerBuilding {
    /**
     * Малює всі шари які є в даному списку, в списку може бути нул елементи
     * @param layerList список шарів
     * @param block блок будівлі
     * @param build сама будівля
     */
    default void draw(@NotNull List<ILayer> layerList, Block block, Building build) {
        layerList.forEach(layer -> {
            if(layer != null) {
                layer.draw(block, build);
            }
        });
    }
}