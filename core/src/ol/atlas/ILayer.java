package ol.atlas;

import mindustry.gen.Building;
import mindustry.world.Block;

/**
 * Шар який використовується для малювання блоку
 */
public interface ILayer {
    /**
     * Малює, по методу зрозуміло
     * @param block блок будівлі
     * @param build будівля, для якої шар малює
     */
    void draw(Block block, Building build);

    /**
     * Завантажує шар, засвичай це спрайти чи перевірка блоку
     * @param block блок для якого шар завантажує
     */
    void load(Block block);
}