package ol.world.blocks.storage;

import mindustry.Vars;
import mindustry.gen.Minerc;
import mindustry.world.blocks.storage.*;
import ol.content.OlItems;
import ol.content.OlUnitTypes;

import java.util.Objects;

public class OlBaseCoreBlock extends CoreBlock {
    public enum EnumCoreType {
        LANDING_CAPSULE;

        public int getMeta() {
            return getMeta(this);
        }

        public static int getMeta(EnumCoreType coreType) {
            return coreType.getMeta();
        }

        public static EnumCoreType getTypeByMetadata(int meta) {
            return values()[meta];
        }
    }

    public EnumCoreType type;
    public OlBaseCoreBlock(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        Objects.requireNonNull(type);
    }

    public class OlBaseCoreBuild extends CoreBuild {
        @Override
        public void updateTile() {
            super.updateTile();
            switch(type) {
                case LANDING_CAPSULE -> {
                    if(items().get(OlItems.grumon) >= 200 && hasAccessibleUnloadPoint()) {
                        items().remove(OlItems.grumon, 200);
                        OlUnitTypes.drillUnit.spawn(team, this);
                        //TODO unit spawn fx
                    }
                }
                default -> {
                    //nothing
                }
            }
        }

        public boolean hasAccessibleUnloadPoint() {
            return Vars.indexer.findTile(team, x, y, 9000, b2 -> {
                return b2 instanceof MiningUnloadPoint.MiningUnloadPointBuild b && b.link == null;
            }) != null;
        }
    }
}