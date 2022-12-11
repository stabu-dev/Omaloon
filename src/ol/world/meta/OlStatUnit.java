package ol.world.meta;

import arc.Core;
import mindustry.world.meta.StatUnit;

import java.util.Locale;

public class OlStatUnit extends StatUnit{
    public static final StatUnit

            pressure = new StatUnit("pressure"),
            kelvins = new StatUnit("kelvins"),
            kilometers = new StatUnit("kilometers");

    public final boolean space;
    public final String name;

    public OlStatUnit(String name, boolean space){
        super(name, space);
        this.name = name;
        this.space = space;
    }

    public OlStatUnit(String name){
        this(name, true);
    }

    public String localized(){
        if(this == none) return "";
        return Core.bundle.get("unit." + name.toLowerCase(Locale.ROOT));
    }
}
