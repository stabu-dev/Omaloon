package omaloon.content;

import arc.*;
import arc.assets.*;
import arc.assets.loaders.*;
import arc.audio.*;

import mindustry.*;

public class OlSounds {
    public static Sound
            theShoot = new Sound(),
            hailRain = new Sound(),
            bigHailstoneHit = new Sound(),
            giantHailstoneFall = new Sound(),
            giantHailstoneHit = new Sound(),
            hammer = new Sound(),
            shelter = new Sound();

    public static void load(){
        theShoot = loadSound("the_shoot");
        hailRain = loadSound("hail_rain");
        bigHailstoneHit = loadSound("big_hailstone_hit");
        giantHailstoneFall = loadSound("giant_hailstone_fall");
        giantHailstoneHit = loadSound("giant_hailstone_hit");
        hammer = loadSound("hammer");
        shelter = loadSound("shelter");
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