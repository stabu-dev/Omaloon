package omaloon.entities.abilities;

import omaloon.annotations.*;
import omaloon.annotations.AutoImplement.*;
import omaloon.annotations.AutoImplement.Inject.*;
import omaloon.core.*;

@AutoImplement
public interface IClockUpdatable extends IClocked{

    @Inject(InjectPosition.Head)
    default void update(){
        {
            int __clock = OlTimer.clock;
            AutoImplement.Util.Param("__clocked_expr__", "__Internal__clockUpdateTime = __clock");
        }
    }
}
