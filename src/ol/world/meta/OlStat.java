package ol.world.meta;

import arc.*;
import arc.struct.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

import java.util.*;

public class OlStat extends Stat {
    public static final Seq<Stat> all = new Seq<>();

    public static final Stat
    magnetic = new Stat("magnetic"),
    damageSpread = new Stat("damage-share", StatCat.general),
    pressureConsume = new Stat("pressure-consume", StatCat.crafting),
    pressureProduction = new Stat("pressure-production", StatCat.crafting),
    maxPressure = new Stat("max-pressure", StatCat.general),
    requirements = new Stat("requirements", StatCat.crafting);
    //averageSurfaceTemperature = new Stat("average-surface-temperature"),
    //rad = new Stat("radius"),
    //orbitRad = new Stat("orbit-radius");

    public final StatCat category;
    public final String name;
    public final int id;

    public OlStat(String name, StatCat category){
        super(name);
        this.category = category;
        this.name = name;
        id = all.size;
        all.add(this);
    }

    public OlStat(String name){
        this(name, StatCat.general);
    }

    public String localized(){
        return Core.bundle.get("stat." + name.toLowerCase(Locale.ROOT));
    }

    @Override
    public int compareTo(Stat o){
        return id - o.id;
    }
}
