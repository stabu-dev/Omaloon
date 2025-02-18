package omaloon.entities.abilities;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import omaloon.annotations.*;
import omaloon.annotations.AutoImplement.*;
import omaloon.annotations.AutoImplement.Inject.*;
import omaloon.core.*;
import omaloon.struct.*;
@SuppressWarnings("UnnecessaryModifier")
@AutoImplement
public interface IClocked{
    public int __Internal__clockUpdateTime = OlTimer.clock;
    default int whenWasUpdated(){
        return __Internal__clockUpdateTime;
    }

}
