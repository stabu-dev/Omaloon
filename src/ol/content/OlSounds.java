package ol.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.audio.*;

import mindustry.*;

public class OlSounds {
    public static Sound

    olShot = new Sound(),
    olCharge = new Sound(),
    piu = new Sound(),
    centrifuge = new Sound(),
    boiler = new Sound();

    public static void load(){
        olShot = loadSound("olShot");
        olCharge = loadSound("olCharge");
        piu = loadSound("piu");
        centrifuge = loadSound("centrifuge");
        boiler = loadSound("boiler");
    }

    private static Sound loadSound(String soundName){
        if(!Vars.headless) {
            String name = "sounds/" + soundName;
            String path = Vars.tree.get(name + ".ogg").exists() ? name + ".ogg" : name + ".mp3";

            Sound sound = new Sound();

            AssetDescriptor<?> desc = Core.assets.load(path, Sound.class, new SoundLoader.SoundParameter(sound));
            desc.errored = Throwable::printStackTrace;

            return sound;

        } else {
            return new Sound();
        }
    }
}
