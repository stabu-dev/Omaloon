package ol.world.blocks.storage;

import arc.graphics.g2d.Draw;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Minerc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.UnitFactory;
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
        public float tmp = 0;

        @Override
        public void draw() {
            super.draw();
            switch(type) {
                case LANDING_CAPSULE -> {
                    if(tmp > 0) {
                        Draw.draw(Layer.blockBuilding, () -> {
                            Drawf.construct(this, OlUnitTypes.drillUnit, 0, tmp/(60f * 4f), 1, Time.globalTime);
                        });
                    }
                }
                default -> {
                    //nothing
                }
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            switch(type) {
                case LANDING_CAPSULE -> {
                    if(items().get(OlItems.grumon) >= 200 && hasAccessibleUnloadPoint()) {
                        if((tmp = tmp + Time.delta) >= 60 * 4) {
                            items().remove(OlItems.grumon, 200);
                            OlUnitTypes.drillUnit.spawn(team, this);
                            //TODO unit spawn fx
                        }
                    } else {
                        tmp = 0;
                    }
                }
                default -> {
                    //nothing
                }
            }
        }

        public boolean hasAccessibleUnloadPoint() {
            return Vars.indexer.findTile(team, x, y, 9000, b2 -> {
                return b2 instanceof UnloadPoint.UnloadPointBuild b && b.link == null;
            }) != null;
        }
    }
}