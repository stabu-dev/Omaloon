package omaloon.type;

import arc.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.type.planet.*;

public class OlPlanet extends BetterPlanet{
    public boolean loadIcon = true;

    public OlPlanet(String name, Planet parent, float radius, int sectorSize){
        super(name, parent, radius, sectorSize);
    }

    @Override
    public void createIcons(MultiPacker packer){
        if(loadIcon){
            Icon.icons.put(name, Core.atlas.getDrawable(name));
            icon = name;
        }
    }
}
