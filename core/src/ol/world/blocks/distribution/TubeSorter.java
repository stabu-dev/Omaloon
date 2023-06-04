package ol.world.blocks.distribution;

import arc.Core;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import mindustry.annotations.Annotations.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import mma.io.*;

import ol.utils.*;

import org.jetbrains.annotations.*;

import static mindustry.Vars.*;

public class TubeSorter extends Block {
    protected float sortScrollItem = 0;

    public TubeSorter(String name) {
        super(name);
        update = false;
        destructible = true;
        underBullets = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
        unloadable = false;
        saveConfig = true;
        clearOnDoubleTap = true;

        config(Item.class, (TubeSorterBuild tile, Item item) -> tile.data.toggle(item));
        configClear((TubeSorterBuild tile) -> tile.data.clear());
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        drawPlanConfigCenter(plan, plan.config, name + "-center", false);
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class TubeSorterBuild extends Building {
        final SortData data = new SortData();

        @Override
        public void configured(Unit player, Object value) {
            super.configured(player, value);

            if (!headless) {
                renderer.minimap.update(tile);
            }
        }

        @Override
        public void draw() {
            super.draw();

            if (data.sortItems.size > 0) {
                Draw.color(content.item(Utils.getByIndex(data.sortItems, ((int) Time.time / 40 + id) % data.sortItems.size)).color);
                Draw.rect(Core.atlas.find(name + "-center"), x, y);
                Draw.color();
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            Building to = getTileTarget(item, source, false);

            return to != null && to.acceptItem(this, item) && to.team == team;
        }

        @Override
        public void handleItem(Building source, Item item) {
            getTileTarget(item, source, true).handleItem(this, item);
        }

        public boolean isSame(Building other) {
            return other != null && other.block.instantTransfer;
        }

        public Building getTileTarget(Item item, Building source, boolean flip) {
            int dir = source.relativeTo(tile.x, tile.y);
            if (dir == -1) return null;
            Building to;

            if ((item != null && data.sortItems.contains(item.id)) == enabled) {
                //prevent 3-chains
                if (isSame(source) && isSame(nearby(dir))) {
                    return null;
                }
                to = nearby(dir);
            } else {
                Building a = nearby(Mathf.mod(dir - 1, 4));
                Building b = nearby(Mathf.mod(dir + 1, 4));
                boolean ac = a != null && !(a.block.instantTransfer && source.block.instantTransfer) &&
                        a.acceptItem(this, item);
                boolean bc = b != null && !(b.block.instantTransfer && source.block.instantTransfer) &&
                        b.acceptItem(this, item);

                if (ac && !bc) {
                    to = a;
                } else if (bc && !ac) {
                    to = b;
                } else if (!bc) {
                    return null;
                } else {
                    to = (rotation & (1 << dir)) == 0 ? a : b;
                    if (flip) rotation ^= (1 << dir);
                }
            }

            return to;
        }

        @Override
        public void buildConfiguration(Table table) {
            ImageButtonStyle style = new ImageButtonStyle(Styles.cleari);
            style.imageDisabledColor = Color.gray;

            ScrollPane itemPane = buildTable(table, content.items(), this::configure, i -> data.sortItems.contains(i.id));
            itemPane.setScrollYForce(sortScrollItem);
            itemPane.update(() -> {
                sortScrollItem = itemPane.getScrollY();
            });

            table.row();
            table.image(Tex.whiteui).size(40f * 4f, 8f).color(Color.gray).left().top();
            table.row();
        }

        public <T extends UnlockableContent> ScrollPane buildTable(Table table, Seq<T> items, Cons<T> consumer, Boolf<T> checked) {

            Table cont = new Table();
            cont.defaults().size(40);

            int i = 0;

            for (T item : items) {
                if (!item.unlockedNow()) continue;

                cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> {
                            consumer.get(item);
                        }).checked(b -> checked.get(item))
                        .update(b -> b.setChecked(checked.get(item)))
                        .get().getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);

                if (i++ % 4 == 3) {
                    cont.row();
                }
            }

            //add extra blank spaces so it looks nice
            if (i % 4 != 0) {
                int remaining = 4 - (i % 4);
                for (int j = 0; j < remaining; j++) {
                    cont.image(Styles.black6);
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            pane.setOverscroll(false, false);
            table.add(pane).maxHeight(Scl.scl(40 * 5));
            return pane;
        }

        @Override
        public byte[] config() {
            return data.toBytes();
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            data.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            data.read(read);

            if (revision == 1) {
                new DirectionalItemBuffer(20).read(read);
            }
        }
    }

    static class SortData {
        private static final ByteWrites byteWrite = new ByteWrites();
        private static final ByteReads byteRead = new ByteReads();
        protected IntSet sortItems = new IntSet();

        public void fromBytes(byte[] bytes) {
            byteRead.setBytes(bytes);
            read(byteRead);
        }

        public byte[] toBytes() {
            byteWrite.reset();
            write(byteWrite);
            return byteWrite.getBytes();
        }

        public void write(Writes write) {
            write.i(0);

            write.i(sortItems.size);
            sortItems.each(itemId -> {
                TypeIO.writeString(write, content.item(itemId).name);
            });
        }

        public void toggle(@NotNull Item item) {
            toggleItem(item.id);
        }

        public void toggleItem(int item) {
            if (!sortItems.add(item)) {
                sortItems.remove(item);
            }
        }

        public void read(Reads read) {
            int rev = read.i();
            read(read, rev);
        }

        private void read(Reads read, int revision) {
            sortItems.clear();

            int itemAmount = read.i();
            for (int i = 0; i < itemAmount; i++) {
                String name = TypeIO.readString(read);
                Item item = content.getByName(ContentType.item, name);
                if (item == null) {
                    name = SaveFileReader.fallback.get(name, name);
                    item = content.getByName(ContentType.item, name);
                    if (item == null) {
                        Log.err("Cannot find item with name \"@\"", name);
                        continue;
                    }
                }
                sortItems.add(item.id);
            }
        }

        public void clear() {
            sortItems.clear();
        }
    }
}
