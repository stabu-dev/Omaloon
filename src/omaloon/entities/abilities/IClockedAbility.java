package omaloon.entities.abilities;

import mindustry.gen.*;
import omaloon.annotations.*;
import omaloon.annotations.AutoImplement.*;
import omaloon.annotations.AutoImplement.Inject.*;
import omaloon.core.*;

@SuppressWarnings({"unused", "UnnecessaryModifier"})
@AutoImplement
public interface IClockedAbility extends IClocked{

    @Inject(InjectPosition.Head)
    default void update(Unit unit){
        {
            int __clock = OlTimer.clock;
            AutoImplement.Util.Param("__clocked_expr__", "__Internal__clockUpdateTime = __clock");
        }
    }
}
