package ol.world.blocks.pressure;

import mindustry.content.*;
import mindustry.world.*;

public interface PressureReplaceable {
    default boolean canBeReplaced(Block other) {
        return other instanceof PressureReplaceable || other == null || other == Blocks.air;
    }
}