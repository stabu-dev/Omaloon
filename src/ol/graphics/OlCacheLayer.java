package ol.graphics;

import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.CacheLayer.*;

public class OlCacheLayer{
    public static ShaderLayer dalaniteLayer;

    public static void init(){
        Log.info("[accent]<FTE + POST (CACHELAYER)>[]");
        dalaniteLayer=new ShaderLayer(OlShaders.dalanite);


        CacheLayer.add(dalaniteLayer);
    }
}
