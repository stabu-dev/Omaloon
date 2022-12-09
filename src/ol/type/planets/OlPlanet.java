package ol.type.planets;

import mindustry.type.Planet;
import mindustry.world.meta.StatUnit;
import ol.world.meta.OlStat;
import ol.world.meta.OlStatUnit;

public class OlPlanet extends Planet {
    public float averageSurfaceTemperature = 0;
    public float rad = 0;
    public float orbitRad = 0;
    public OlPlanet(String name, Planet parent, float radius, int sectorSize) {
        super(name, parent, radius, sectorSize);
    }
    @Override
    public void setStats(){
        stats.add(OlStat.averageSurfaceTemperature, averageSurfaceTemperature, OlStatUnit.kelvins);
        stats.add(OlStat.rad, rad, OlStatUnit.kilometers);
        if(!(orbitRad == 0)){
            stats.add(OlStat.orbitRad, orbitRad, OlStatUnit.kilometers);
        }
    }
}
