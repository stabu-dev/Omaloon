package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;

import static mindustry.Vars.*;

public class TubeSorter extends Block{
    // TODO setting?
    public float colorSwitchTime = 60f;

    public @Load("@-item") TextureRegion itemRegion;

    public TubeSorter(String name){
        super(name);
        update = false;
        hasItems = true;
        destructible = true;
        underBullets = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
        unloadable = false;
        saveConfig = true;

        config(TubeSorterEntry.class, (TubeSorterBuild build, TubeSorterEntry filter) -> {
            build.selected.set(filter.selected);
        });
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config instanceof TubeSorterEntry entry && entry.selected.length > 0){
            int id = Mathf.clamp(Mathf.floor(Time.time % (colorSwitchTime * entry.selected.length) / colorSwitchTime), 0, entry.selected.length - 1);
            Item item = entry.selected[id];
            if(item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, plan.drawx(), plan.drawy());
                Draw.color();
            }
        }
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
            table.table(Styles.black6, items -> {
                items.defaults().size(40);
                int i = 0;
                for(Item item : content.items().select(item -> item.unlocked() && item.isOnPlanet(state.getPlanet()) && !item.hidden)){
                    ImageButton button = items.button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize, 0f, 40f), () -> {
//                        if(closeSelect) control.input.config.hideConfig();
                        if (!selected.remove(item)) {
                            selected.add(item);
                        }
                        configure(new TubeSorterEntry(selected.toArray(Item.class)));
                    }).tooltip(item.localizedName).get();
//                    button.changed(() -> consumer.get(button.isChecked() ? item : null));
                    button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
                    button.update(() -> button.setChecked(selected.contains(item)));

                    if(i++ % 4 == (4 - 1)){
                        items.row();
                    }
                }
            }).margin(10);
        }

        @Override
        public TubeSorterEntry config(){
            return new TubeSorterEntry(selected.toArray(Item.class));
        }

        @Override
        public void configured(Unit player, Object value){
            super.configured(player, value);

            if(!headless){
                renderer.minimap.update(tile);
            }
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

            if((item != null && selected.contains(item) && enabled)){
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
            for(int i = 0; i < size; i++) {
                int id = read.i();
                if (content.item(id) != null) selected.add(content.item(id));
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.b(selected.size);
            selected.each(item -> write.i(item.id));
        }
    }

    public static class TubeSorterEntry {
        public Item[] selected;

        public TubeSorterEntry(Item[] selected){
            this.selected = selected;
        }
    }
}
