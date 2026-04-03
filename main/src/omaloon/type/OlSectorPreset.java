package omaloon.type;

import arc.*;
import mindustry.gen.*;
import mindustry.type.*;

public class OlSectorPreset extends SectorPreset{
    public OlSectorPreset(String name, Planet planet, int sector) {
        super(name, planet, sector);
    }

    @Override
    public void loadIcon(){
        fullIcon = Core.atlas.find(fullOverride == null ? "" : fullOverride,
            Core.atlas.find(getContentType().name() + "-" + name + "-full",
                Core.atlas.find(name + "-full",
                    Core.atlas.find(name,
                        Core.atlas.find(getContentType().name() + "-" + name,
                            Core.atlas.find(name + "1")
                        )
                    )
                )
            )
        );
        uiIcon = Core.atlas.find(getContentType().name() + "-" + name + "-ui", fullIcon);
        if (Icon.terrain != null && !fullIcon.found()) {
            uiIcon = fullIcon = Core.atlas.find(name, Icon.terrain.getRegion());
        }
    }
}
