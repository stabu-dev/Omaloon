package omaloon.tools;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.g2d.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import arc.util.io.*;

import mindustry.async.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import mindustry.world.blocks.*;

import omaloon.*;
import omaloon.gen.*;
import omaloon.tools.GenAtlas.*;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

import static mindustry.Vars.*;

/**
 * Main entry point of the tools' module. This must only affect the main project's asset directory.
 * @author GlennFolker
 */
public final class Tools{
    public static OmaloonMod thisMod;
    public static LoadedMod mod;
    public static ModMeta meta;

    public static final Fi
    assetsDir, spritesDir;

    public static GenAtlas atlas;

    private static final TaskQueue runs = new TaskQueue();
    private static final IntSet[] initialized = new IntSet[ContentType.all.length];
    private static final IntSet[] loaded = new IntSet[ContentType.all.length];

    static{
        assetsDir = new Fi(Paths.get("").toFile());
        spritesDir = assetsDir.child("sprites");

        for(var type : ContentType.all){
            int i = type.ordinal();
            synchronized(initialized){
                initialized[i] = new IntSet();
            }
            synchronized(loaded){
                loaded[i] = new IntSet();
            }
        }
    }

    private Tools(){
    }

    public static void main(String[] args){
        Log.logger = new NoopLogHandler();

        headless = true;
        Core.app = new MockApplication(){
            @Override
            public void post(Runnable runnable){
                runs.post(runnable);
            }
        };
        Core.files = new MockFiles();
        Core.assets = new AssetManager(tree = new FileTree());
        Core.settings = new Settings();
        Core.atlas = atlas = new GenAtlas();

        asyncCore = new AsyncCore();
        state = new GameState();
        mods = new Mods();

        content = new ContentLoader();
        content.createBaseContent();

        thisMod = new OmaloonMod(true);

        meta = new ModMeta(){{
            name = System.getProperty("currentModName");
        }};
        mod = new LoadedMod(null, null, thisMod, Tools.class.getClassLoader(), meta);

        Reflect.<Seq<LoadedMod>>get(Mods.class, mods, "mods").add(mod);
        Reflect.<ObjectMap<Class<?>, ModMeta>>get(Mods.class, mods, "metas").put(OmaloonMod.class, meta);

        addRegions();
        atlas.clear = atlas.find("clear");

        content.setCurrentMod(mod);
        thisMod.loadContent();
        content.setCurrentMod(null);

        Log.logger = new DefaultLogHandler();
        loadLogger();

        runs.run();
        Processors.process();
        runs.run();

        //generate icons
        try{
            Fi iconfile = assetsDir.child("icons").child(meta.name + "-icons.properties");
            iconfile.parent().mkdirs();

            OrderedMap<String, String> map = new OrderedMap<>();
            if(iconfile.exists()){
                PropertiesUtils.load(map, iconfile.reader(256));
            }

            ObjectMap<String, String> nameToKey = new ObjectMap<>();
            map.each((key, val) -> {
                String[] parts = val.split("\\|");
                if(parts.length > 0) nameToKey.put(parts[0], key);
            });

            Seq<UnlockableContent> cont = Seq.withArrays(content.blocks(), content.items(), content.liquids(), content.units(), content.statusEffects());
            cont.removeAll(c -> c.minfo.mod != mod || c instanceof ConstructBlock || c == Blocks.air || (c instanceof UnitType t && t.internal));

            int minid = 0xF8FF;
            for(String key : map.keys()){
                try{
                    minid = Math.min(Integer.parseInt(key) - 1, minid);
                }catch(NumberFormatException ignored){
                }
            }

            boolean changed = false;
            for(UnlockableContent c : cont){
                String newValue = c.name + "|" + texname(c);
                String key = nameToKey.get(c.name);

                if(key != null){
                    if(!map.get(key).equals(newValue)){
                        map.put(key, newValue);
                        changed = true;
                    }
                }else{
                    map.put(minid + "", newValue);
                    minid--;
                    changed = true;
                }
            }

            if(changed){
                Writer writer = iconfile.writer(false);
                for(String k : map.keys()){
                    writer.write(k + "=" + map.get(k) + "\n");
                }
                writer.close();
            }
        }catch(java.io.IOException e){
            throw new RuntimeException(e);
        }

        atlas.dispose();
    }

    private static String texname(UnlockableContent c){
        return c.name + "-ui";
    }

    private static void addRegions(){
        Log.info("Adding regions...");
        Time.mark();

        ExecutorService exec = Executors.newCachedThreadPool();

        spritesDir.walk(path -> {
            if(!path.extEquals("png")) return;
            exec.submit(() -> atlas.addRegion(path));
        });

        Threads.await(exec);
        Log.info("Total time to add regions: @ms", Time.elapsed());
    }

    @SuppressWarnings("all")
    public static boolean init(Content content){
        synchronized(initialized){
            boolean should = initialized[content.getContentType().ordinal()].add(content.id);
            if(should) content.init();

            return should;
        }
    }

    @SuppressWarnings("all")
    public static boolean load(Content content){
        synchronized(loaded){
            boolean should = loaded[content.getContentType().ordinal()].add(content.id);
            if(should){
                content.load();
                if(content instanceof MappableContent c) OlContentRegionRegistry.load(c);
            }

            return should;
        }
    }

    public static GenRegion conv(TextureRegion region){
        return (GenRegion)region;
    }
}