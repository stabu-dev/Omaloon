package ol.content;

import arc.Core;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.audio.*;
import arc.struct.*;

import mindustry.*;

import java.lang.reflect.*;

public class OlSounds {
    public static Sound
            boil = new Sound(),
            piu = new Sound(),
            freezingShot = new Sound(),
            freezingCharge = new Sound();

    public static void load() {
        Class<?> c = OlSounds.class;
        Seq<Field> fields = new Seq<>(c.getFields());
        fields.filter(f -> Sound.class.equals(f.getType()));
        try {
            for (Field f : fields) f.set(null, loadSound(f.getName()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Sound loadSound(String soundName){
        if(!Vars.headless){
            String name = "sounds/" + soundName;
            String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";

            Sound sound = new Sound();

            AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundLoader.SoundParameter(sound));
            desc.errored = Throwable::printStackTrace;
            return sound;
        }else return new Sound();
    }
}
