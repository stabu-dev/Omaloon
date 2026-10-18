package omaloon.type;

import arc.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.Weather.*;

import static mindustry.Vars.*;

public class OlSectorPreset extends SectorPreset{
    /** Random weather that starts once the sector is captured, as world processors stop then. */
    public Seq<WeatherEntry> capturedWeather = new Seq<>();

    public OlSectorPreset(String name, Planet planet, int sector) {
        super(name, planet, sector);
    }

    public static void captured(SectorCaptureEvent event){
        if(!event.initialCapture || !(event.sector.preset instanceof OlSectorPreset preset)) return;

        for(WeatherEntry e : preset.capturedWeather){
            state.rules.weather.add(new WeatherEntry(e.weather, e.minFrequency, e.maxFrequency, e.minDuration, e.maxDuration));
        }
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
