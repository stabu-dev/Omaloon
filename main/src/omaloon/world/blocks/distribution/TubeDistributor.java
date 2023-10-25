package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;

import arc.math.Interp;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import static mindustry.Vars.itemSize;

public class TubeDistributor extends Router {
    public TextureRegion rotorRegion, bottomRegion;

    public TubeDistributor(String name) {
        super(name);
    }

    public TextureRegion loadRegion(String prefix) {
        return Core.atlas.find(name + prefix);
    }

    @Override
    public void load() {
        super.load();
        rotorRegion = loadRegion("-rotator");
        bottomRegion = loadRegion("-bottom");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[] {bottomRegion, rotorRegion, region};
    }

    public class TubeDistributorBuild extends RouterBuild {
        public Item lastItem;
        public Tile lastInput;
        public float time, rot;

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                lastItem = items.first();
            }

            if(lastItem != null){
                time += 1f / speed * delta();

                Building target = getTileTarget(lastItem, lastInput, false);

                if(target != null && (time >= 1f)){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                }

                if (items.total() > 0 && !Vars.state.isPaused()) {
                    rot += speed * delta();
                } else if (!(rot > 0)) {
                    rot = 0;
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return team == source.team && lastItem == null && items.total() == 0;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            lastItem = item;
            time = 0f;
            lastInput = source.tile();
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == lastItem){
                lastItem = null;
            }
            return result;
        }

        public void drawItem() {
            Building target = getTileTarget(lastItem, lastInput, false);
            if (lastInput != null && target != null) {
                boolean isf = lastInput.build.rotation == target.rotation;
                boolean alignment = target.rotation == 0 || target.rotation == 2;
                float ox, oy, s = size * 4, s2 = s * 2;

                if (alignment) {
                    if (isf) {
                        oy = (float) Math.sin(Math.PI * time) / 2.3f * s;
                        ox = (time * s2 - s) * (target.rotation == 0 ? 1 : -1);
                    } else {
                        oy = target.angleTo(lastInput) == 1 ? (time * -s + s) : (time * s - s);
                        ox = time * s * (target.rotation == 0 ? 1 : -1);
                    }
                } else {
                    if (isf) {
                        ox = (float) Math.sin(Math.PI * time) / 2.3f * s;
                        oy = (time * s2 - s) * (target.rotation == 1 ? 1 : -1);
                    } else {
                        ox = target.angleTo(lastInput) == 0 ? (time * -s + s) : (time * s - s);
                        oy = time * s * (target.rotation == 1 ? 1 : -1);
                    }
                }

                Draw.rect(lastItem.fullIcon, x + ox, y + oy, itemSize, itemSize);
            }
        }

        @Override
        public void draw() {
            super.draw();
            Draw.rect(bottomRegion, x, y);
            if (lastItem != null) {
                drawItem();
            }
            Drawf.spinSprite(rotorRegion, x, y, rot % 360);
            Draw.rect(region, x, y);
        }


        public Building getTileTarget(Item item, Tile from, boolean set){
            int counter = rotation;
            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(set) rotation = ((byte)((rotation + 1) % proximity.size));
                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }
            return null;
        }
    }
}