package ol.world.meta;

import arc.Core;
import arc.struct.Seq;
import mindustry.world.meta.StatCat;

public class OlStatCat extends StatCat {
    public static final Seq<StatCat> all = new Seq<>();

    public static final StatCat

    pressure = new StatCat("pressure");

    public final String name;
    public final int id;

    public OlStatCat(String name){
        super(name);
        this.name = name;
        id = all.size;
        all.add(this);
    }

    public String localized(){
        return Core.bundle.get("category." + name);
    }

    @Override
    public int compareTo(StatCat o){
        return id - o.id;
    }
}
