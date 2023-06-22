package ol.world.blocks.storage;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.block.instance.AdvancedBlock;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import ol.entity.MiningUnitEntity;
import ol.multiitem.MultiItemConfig;
import ol.multiitem.MultiItemData;
import ol.multiitem.MultiItemSelection;
import static mindustry.Vars.*;

public class MiningUnloadPoint extends AdvancedBlock {
    public MiningUnloadPoint(String name) {
        super(name);
        hasItems = true;
        configurable = true;
        update = true;

        MultiItemConfig.configure(this, (MiningUnloadPointBuild build) -> {
            return build.data;
        });
    }

    public class MiningUnloadPointBuild extends AdvancedBuild {
        public final MultiItemData data = new MultiItemData();
        public MiningUnitEntity link;
        public int tick = 0;

        public void synchronization() {
            if(link != null && link.link != this) {
                link = null;
            }
        }

        public boolean canAcceptItem(Item item) {
            return data.isToggled(item) || data.length() == 0;
        }

        public Tile findOre(Drill drill) {
            for(var item : content.items()) {
                if(canAcceptItem(item)) {
                    var tile = indexer.findClosestOre(x, y, item);
                    if(tile != null && tile.build == null && drill.canMine(tile)) {
                        return tile;
                    }
                }
            }

            return null;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if(link != null) {
                Drawf.line(Color.green, x, y, link.x, link.y);
                Drawf.square(x, y, size * 4, 45, Color.green);
                Drawf.square(link.x, link.y, link.type.hitSize/2, 45, Color.green);
            } else {
                Drawf.square(x, y, size * 4, 45, Color.red);
            }
        }

        @Override
        public void updateTile() {
            dump(null);
            if(tick++ % 30 == 0) {
                synchronization();
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            MultiItemSelection.buildTable(table, data);
        }

        @Override
        public void write(Writes write) {
            data.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            data.read(read);
        }

        @Override
        public int[] config() {
            return data.config();
        }
    }
}
