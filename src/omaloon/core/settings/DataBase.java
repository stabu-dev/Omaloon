package omaloon.core.settings;

import mindustry.content.TechTree.*;
import mindustry.type.*;

public class DataBase{
    public static void resetSaves(Planet planet){
        planet.sectors.each(sector -> {
            if(!sector.hasSave()) return;
            sector.save.delete();
            sector.save = null;
        });
    }

    public static void resetTree(TechNode root){
        root.reset();
        root.content.clearUnlock();
        root.children.each(DataBase::resetTree);
    }
}
