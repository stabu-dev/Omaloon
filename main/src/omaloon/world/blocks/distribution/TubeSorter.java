package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.ui.*;

import static mindustry.Vars.*;

public class TubeSorter extends Block{
    // TODO setting?
    public float colorSwitchTime = 60f;

    public @Load("@-item") TextureRegion itemRegion;

    public TubeSorter(String name){
        super(name);
        update = false;
        destructible = true;
        underBullets = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
        unloadable = false;
        saveConfig = true;

        config(Item[].class, (TubeSorterBuild build, Item[] items) -> build.selected.set(items));
        config(Object[].class, (TubeSorterBuild build, Object[] items) -> build.selected.set(items(items)));
        configClear((TubeSorterBuild build) -> build.selected.clear());
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        Item[] items = plan.config instanceof Object[] config ? items(config) : null;
        if(items != null && items.length > 0){
            int id = Mathf.clamp(Mathf.floor(Time.time % (colorSwitchTime * items.length) / colorSwitchTime), 0, items.length - 1);
            Item item = items[id];
            if(item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, plan.drawx(), plan.drawy());
                Draw.color();
            }
        }
    }

    static Item[] items(Object[] config){
        Seq<Item> out = new Seq<>(Item.class);
        for(Object o : config) if(o instanceof Item item) out.add(item);
        return out.toArray();
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class TubeSorterBuild extends Building{
        public Seq<Item> selected = new Seq<>();

        @Override
        public boolean acceptItem(Building source, Item item){
            Building to = getTileTarget(item, source, false);
            return to != null && to.acceptItem(this, item) && to.team == team;
        }

        @Override
        public void buildConfiguration(Table table){
            ItemMultiSelection.buildTable(TubeSorter.this, table, content.items(), selected::contains, item -> {
                Seq<Item> next = selected.copy();
                if(!next.remove(item)) next.add(item);
                configure(next.toArray(Item.class));
            }, selectionRows, selectionColumns);
        }

        @Override
        public Item[] config(){
            return selected.toArray(Item.class);
        }

        @Override
        public void configured(Unit player, Object value){
            super.configured(player, value);

            if(!headless){
                renderer.minimap.update(tile);
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other == self()){
                deselect();
                configure(selected.isEmpty() ? content.items().select(ItemMultiSelection::available).toArray(Item.class) : null);
                return false;
            }
            return super.onConfigureBuildTapped(other);
        }

        @Override
        public void draw(){
            super.draw();

            if(selected.size > 0){
                Item item = selected.getFrac(((Time.time + id) % (colorSwitchTime * selected.size)) / (colorSwitchTime * selected.size));
                if(item != null){
                    Draw.color(item.color);
                    Draw.rect(itemRegion, x, y);
                    Draw.color();
                }
            }
        }

        public Building getTileTarget(Item item, Building source, boolean flip){
            int dir = source.relativeTo(tile.x, tile.y);
            if(dir == -1) return null;
            Building to;

            if((item != null && selected.contains(item)) == enabled){
                // Prevent 3-chains
                if(isSame(source) && isSame(nearby(dir))){
                    return null;
                }
                to = nearby(dir);
            }else{
                Building a = nearby(Mathf.mod(dir - 1, 4));
                Building b = nearby(Mathf.mod(dir + 1, 4));
                boolean ac = a != null && !(a.block.instantTransfer && source.block.instantTransfer) &&
                a.acceptItem(this, item);
                boolean bc = b != null && !(b.block.instantTransfer && source.block.instantTransfer) &&
                b.acceptItem(this, item);

                if(ac && !bc){
                    to = a;
                }else if(bc && !ac){
                    to = b;
                }else if(!bc){
                    return null;
                }else{
                    to = (rotation & (1 << dir)) == 0 ? a : b;
                    if(flip) rotation ^= (1 << dir);
                }
            }

            return to;
        }

        @Override
        public void handleItem(Building source, Item item){
            getTileTarget(item, source, true).handleItem(this, item);
        }

        public boolean isSame(Building other){
            return other != null && other.block.instantTransfer;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            byte size = read.b();
            for(int i = 0; i < size; i++){
                int id = read.i();
                Item item = content.item(id);
                if(item != null) selected.add(item);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.b(selected.size);
            selected.each(item -> write.i(item.id));
        }
    }
}
