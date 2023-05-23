package ol.atlas;

import mindustry.world.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Блок для шарів, він створенний, щоб їх отримувати, а потів завантажувати
 */
public interface ILayerBlock {
    /**
     * Отримує список шарів, значення має бути NotNull
     * @return список шарів
     */
    @NotNull List<ILayer> getLayers();

    /**
     * Завантажує шари блоку, засвичай шари так завантажують спрайти
     * @param block блок
     */
    default void loadLayers(Block block) {
        getLayers().forEach((layer) -> {
            if(layer != null) {
                layer.load(block);
            }
        });
    }
}