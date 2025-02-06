package omaloon.content;

import arc.*;
import arc.audio.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import omaloon.content.blocks.*;

/**
 * Manages music, including vanilla and custom tracks.
 * @author stabu_
 */
public class OlMusics{
    private static final ObjectMap<String, Seq<Music>> musicSets = new ObjectMap<>();

    public static Music
    // Vanilla
    launch, land,
    // Omaloon launch
    orbital,
    // Glasmore music
    glLand,
    // Ambient
    chained, darkPurity, wisdom, space, sundown,
    // Dark
    fragile, solidFire, soredLuna,
    // Boss
    buryAlive, chaoticFlames, liquefy, piercingLine;

    public static void load(){
        initializeMusics();
        initializeMusicSets();
        setupEventHandlers();
    }

    /** Initializes individual music tracks. */
    private static void initializeMusics(){
        // Vanilla
        launch = Musics.launch;
        land = Musics.land;

        // Omaloon
        orbital = loadMusic("orbital");

        // Glasmore
        glLand = loadMusic("landings/glasmore-land");

        // Load Glasmore musics
        String[] ambientTracks = {"chained", "darkPurity", "wisdom", "space", "sundown"};
        String[] darkTracks = {"fragile", "solidFire", "soredLuna"};
        String[] bossTracks = {"buryAlive", "chaoticFlames", "liquefy", "piercingLine"};

        loadMusicSet("glasmore/ambient/", ambientTracks);
        loadMusicSet("glasmore/dark/", darkTracks);
        loadMusicSet("glasmore/boss/", bossTracks);
    }

    /**
     * Loads a set of music tracks from a specified base path.
     * @param basePath Base path for the music files.
     * @param trackNames Array of track names to load.
     */
    private static void loadMusicSet(String basePath, String[] trackNames){
        for(String track : trackNames){
            try{
                Music music = loadMusic(basePath + track);
                OlMusics.class.getField(track).set(null, music);
            }catch(Exception e){
                Log.err("Failed to load music: " + track, e);
            }
        }
    }

    /** Initializes music sets for different game scenarios. */
    private static void initializeMusicSets(){
        musicSets.put("vanillaAmbient", new Seq<>(Vars.control.sound.ambientMusic));
        musicSets.put("vanillaDark", new Seq<>(Vars.control.sound.darkMusic));
        musicSets.put("vanillaBoss", new Seq<>(Vars.control.sound.bossMusic));

        musicSets.put("glasmoreAmbient", Seq.with(chained, darkPurity, wisdom, space, sundown));
        musicSets.put("glasmoreDark", Seq.with(fragile, solidFire, soredLuna));
        musicSets.put("glasmoreBoss", Seq.with(buryAlive, chaoticFlames, liquefy, piercingLine));
    }

    /** Sets up event handlers for updating music based on game events. */
    private static void setupEventHandlers(){
        Events.run(EventType.Trigger.update, OlMusics::updateLaunchMusic);
        Events.on(WorldLoadEvent.class, e -> {
            updateLandMusic();
            updatePlanetMusic();
        });
    }

    /** Updates launch music based on current planet. */
    private static void updateLaunchMusic(){
        Musics.launch = (Vars.ui.planet.state.planet == OlPlanets.omaloon || Vars.ui.planet.state.planet == OlPlanets.glasmore)
        ? orbital
        : launch;
    }

    /** Updates landing music based on core block type. */
    private static void updateLandMusic(){
        Vars.state.rules.defaultTeam.cores().each(core ->
        Musics.land = (core.block == OlStorageBlocks.landingCapsule || core.block == OlStorageBlocks.coreFloe)
        ? glLand
        : land);
    }

    /** Updates planet music sets based on a current planet. */
    private static void updatePlanetMusic(){
        if(Vars.state.rules.planet != Planets.sun){
            String prefix = Vars.state.rules.planet == OlPlanets.glasmore ? "glasmore" : "vanilla";
            setMusicSet(prefix + "Ambient", Vars.control.sound.ambientMusic);
            setMusicSet(prefix + "Dark", Vars.control.sound.darkMusic);
            setMusicSet(prefix + "Boss", Vars.control.sound.bossMusic);
        }else{
            // For 'any' environment (Planets.sun), mix mod and vanilla music
            mixMusic();
        }
    }

    /** Mixes vanilla and mod music sets. */
    private static void mixMusic(){
        mixMusicSets("vanillaAmbient", "glasmoreAmbient", Vars.control.sound.ambientMusic);
        mixMusicSets("vanillaDark", "glasmoreDark", Vars.control.sound.darkMusic);
        mixMusicSets("vanillaBoss", "glasmoreBoss", Vars.control.sound.bossMusic);
    }

    /**
     * Mixes two music sets and assigns the result to a target set.
     * @param target Target sequence to store the mixed music.
     */
    private static void mixMusicSets(String vanillaSetName, String modSetName, Seq<Music> target){
        Seq<Music> vanillaSet = musicSets.get(vanillaSetName);
        Seq<Music> modSet = musicSets.get(modSetName);
        if(vanillaSet != null && modSet != null){
            target.clear();
            target.addAll(vanillaSet);
            target.addAll(modSet);
        }
    }

    /**
     * Sets a music set to a target sequence.
     * @param setName Name of the music set to use.
     * @param target Target sequence to update.
     */
    private static void setMusicSet(String setName, Seq<Music> target){
        Seq<Music> set = musicSets.get(setName);
        if(set != null){
            target.set(set);
        }
    }

    /** Loads a music file from the game's asset tree. */
    private static Music loadMusic(String name){
        return Vars.tree.loadMusic(name);
    }
}