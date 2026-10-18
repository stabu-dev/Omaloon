package omaloon.core;

import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.game.Saves.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import omaloon.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Resets the campaign on a planet, which Omaloon does whenever the mod is updated, since old saves may be incompatible with new content.
 * @author stabu_
 */
public class OlCampaignReset{
    /** Mod version that was loaded on the last launch. */
    private static final String versionKey = "omaloon-last-version";

    /** Records the running mod version. @return whether it differs from the one stored on the last launch. */
    public static boolean checkUpdate(){
        LoadedMod mod = mods.getMod("omaloon");
        if(mod == null) return false;

        String current = mod.meta.version;
        if(current.equals(settings.getString(versionKey, ""))) return false;

        settings.put(versionKey, current);
        return true;
    }

    /** Deletes all campaign progress on the planet: sector saves, sector info, stats and research. */
    public static void clear(Planet planet){
        universe.clearLoadoutInfo();

        for(TechNode node : TechTree.all){
            if(node.rootNode == planet.techTree) node.reset();
        }

        content.each(c -> {
            if(c instanceof UnlockableContent u && u.databaseTabs.contains(planet)){
                u.clearUnlock();
            }
        });
        settings.remove("unlocks");

        planet.clearStats();

        boolean any = false;
        for(Sector sector : planet.sectors){
            sector.clearInfo();
            if(sector.save != null){
                any = true;
                sector.save.delete();
                sector.save = null;
            }
        }
        if(any) planet.reloadMeshAsync();

        for(SaveSlot slot : control.saves.getSaveSlots().copy()){
            if(slot.isSector() && slot.getSector().planet == planet){
                slot.delete();
            }
        }
    }
}
