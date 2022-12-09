package ol.content;

import arc.Core;
import arc.assets.AssetDescriptor;
import arc.assets.loaders.SoundLoader;
import arc.audio.Sound;
import mindustry.Vars;

public class OlSounds {
    public static Sound
    olShot = new Sound(),
    olCharge = new Sound(),
    centrifuge = new Sound(),
    boiler = new Sound();
    //zoneShot = new Sound();

    public static void load(){
        olShot = loadSound("olShot");
        olCharge = loadSound("olCharge");
        centrifuge = loadSound("centrifuge");
        boiler = loadSound("boiler");
        //zoneShot = loadSound("zoneShot");
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
