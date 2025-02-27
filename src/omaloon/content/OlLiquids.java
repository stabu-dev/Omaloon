package omaloon.content;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.type.liquid.*;
import omaloon.world.meta.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;

import java.util.*;

import static arc.graphics.Color.valueOf;

public class OlLiquids{
    public static final LiquidInfo defaultLiquidInfo = new LiquidInfo(8f, 1f);//TODO maybe move into LiquidInfо?
    public static Liquid
        glacium, tiredGlacium,

    end;

    private static LiquidInfo[] liquidInfos = new LiquidInfo[0];
    private static boolean __was_omaloon_stats__ = false;

    public static void load(){
        glacium = new CrystalLiquid("glacium", valueOf("5e929d")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(Liquids.water);
        }};

        tiredGlacium = new Liquid("tired-glacium", valueOf("456c74")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            canStayOn.add(Liquids.water);
        }};
        initLiquidInfo();

        Events.on(EventType.ContentInitEvent.class, e -> {
            changeDisplayLiquidStats(Core.settings.getBool("omaloon-display-liquid-stats", true));
        });
    }

    private static void initLiquidInfo(){//TODO move into ApplicationListener.init
        setLiquidInfo(Liquids.water, 1000, 10);
        setLiquidInfo(Liquids.slag, 1600, 250);
        setLiquidInfo(Liquids.oil, 700, 50);
        setLiquidInfo(Liquids.cryofluid, 200, 1.2f);
        setLiquidInfo(glacium, 1300, 13);
        setLiquidInfo(tiredGlacium, 1300, 13);
    }

    public static void setLiquidInfo(Liquid liquid, float density, float viscosity){
        if(liquidInfos.length == 0){
            liquidInfos = new LiquidInfo[Vars.content.liquids().size];
            Arrays.fill(liquidInfos, defaultLiquidInfo);
        }
        if(liquid.id >= liquidInfos.length){
            LiquidInfo[] old = liquidInfos;
            int newLen = old.length + 4;
            LiquidInfo[] newArr = new LiquidInfo[newLen];
            System.arraycopy(old, 0, newArr, 0, old.length);
            liquidInfos = newArr;
            for(int i = old.length + 1; i < newLen; i++){
                newArr[i] = defaultLiquidInfo;
            }
        }
        liquidInfos[liquid.id] = new LiquidInfo(density, viscosity);
    }

    @Deprecated
    public static float getDensity(@Nullable Liquid liquid){
        return liquidInfo(liquid).density;
    }

    @NotNull
    public static LiquidInfo liquidInfo(@Nullable Liquid liquid){
        if(liquid == null) return defaultLiquidInfo;
        if(liquid.id >= liquidInfos.length) return defaultLiquidInfo;
        return liquidInfos[liquid.id];
    }

    @Deprecated
    public static float getViscosity(@Nullable Liquid liquid){
        return liquidInfo(liquid).viscosity;
    }

    public static void changeDisplayLiquidStats(boolean enabled){
        if(__was_omaloon_stats__ == enabled) return;
        __was_omaloon_stats__ = enabled;
        if(enabled){
            addOmaloonLiquidStats();
        }else{
            removeOmaloonLiquidStats();
        }

    }

    private static void addOmaloonLiquidStats(){
        for(int id = 0; id < liquidInfos.length; id++){
            LiquidInfo info = liquidInfos[id];
            if(info == null || info == defaultLiquidInfo) continue;
            Liquid liquid = Vars.content.liquid(id);

            liquid.stats.add(OlStats.density, info.density, OlStats.densityUnit);

            liquid.checkStats();
            liquid.stats.remove(Stat.viscosity);
            liquid.stats.add(Stat.viscosity, Core.bundle.get("stat.omaloon-viscosity"),
                liquid.viscosity * 100f,
                Strings.autoFixed(info.viscosity / 60f, 2) + " " + OlStats.viscosityUnit.localized()
            );
        }
    }

    private static void removeOmaloonLiquidStats(){
        for(int id = 0; id < liquidInfos.length; id++){
            LiquidInfo info = liquidInfos[id];
            if(info == null || info == defaultLiquidInfo) continue;
            Liquid liquid = Vars.content.liquid(id);

            liquid.checkStats();
            liquid.stats.remove(OlStats.density);
            liquid.stats.remove(Stat.viscosity);
            liquid.stats.addPercent(Stat.viscosity, liquid.viscosity);
        }
    }
}
