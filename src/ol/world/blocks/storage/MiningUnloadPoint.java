package ol.world.blocks.storage;

import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.block.instance.AdvancedBlock;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import ol.entity.MiningUnitEntity;
import ol.multiitem.MultiItemConfig;
import ol.multiitem.MultiItemData;
import ol.multiitem.MultiItemSelection;
import ol.utils.Utils;

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
        public int index = -1;
        public int tick = 0;

        public void synchronization() {
            if(link != null && link.link != this) {
                link = null;
            }
        }

        public boolean canAcceptItem(Item item) {
            return data.isToggled(item) || data.length() == 0;
        }

        public Seq<Item> canAccept() {
            Seq<Item> items1 = new Seq<>();
            for(var it : content.items()) {
                if(canAcceptItem(it)) {
                    items1.add(it);
                }
            }
            return items1;
        }

        public float range() {
            return 13;
        }

        public Tile findTile(Item item, Drill drill) {
            var tile = indexer.findClosestOre(x, y, item);
            float dist = tile == null ? 0 : Utils.distance(tileX(), tileY(), tile.x, tile.y);
            //Log.info("Distance between " + item.name + " ore and this: " + dist);
            if(tile != null && dist <= range() && tile.build == null && (drill == null || drill.canMine(tile))) {
                return tile;
            } else {
                return null;
            }
        }

        public Tile findOre(Drill drill) {
            try {
                var list = canAccept();
                var item = list.get(++index % list.size);
                if(canAcceptItem(item)) {
                    var tile = findTile(item, drill);
                    if(tile != null) {
                        return tile;
                    } else {
                        return findOre(drill);
                    }
                }
            } catch(StackOverflowError ignored) {
            }

            return null;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, range() * 8, team.color);
            if(link != null) {
                Drawf.line(Color.green, x, y, link.x, link.y);
                Drawf.square(x, y, size * 4, 45, Color.green);
                Drawf.square(link.x, link.y, link.type.hitSize == 0 ? Math.min(link.type.region.width,
                        link.type.region.height) / 6f : link.type.hitSize, 45, Color.green);
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
            table.row();
            table.table(bg -> {
                bg.setBackground(Styles.black5);
            }).margin(6).left().update(t -> {
                t.clearChildren();
                var list = canAccept();
                var it = list.get((index == -1 ? 0 : index) % list.size);
                t.add(new Image(it.uiIcon).setScaling(Scaling.fit)).tooltip(it.localizedName);
            });
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
