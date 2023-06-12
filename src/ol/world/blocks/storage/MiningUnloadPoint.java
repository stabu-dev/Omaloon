package ol.world.blocks.storage;

import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.block.instance.AdvancedBlock;
import mindustry.type.Item;
import ol.multiitem.MultiItemConfig;
import ol.multiitem.MultiItemData;
import ol.multiitem.MultiItemSelection;

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

        public boolean canAcceptItem(Item item) {
            return data.isToggled(item) || data.length() == 0;
        }

        @Override
        public void updateTile() {
            dump(null);
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
