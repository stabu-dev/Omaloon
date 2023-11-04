package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.util.Eachable;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.draw.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class TubeDistributor extends Router {
    public DrawBlock drawer = new DrawDefault();
    public TextureRegion rotorRegion;

    public TubeDistributor(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
        rotorRegion = atlas.find(name + "-rotator");
        uiIcon = atlas.find(name + "-icon");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        Draw.rect(uiIcon, plan.drawx(), plan.drawy());
    }

    public class TubeDistributorBuild extends RouterBuild {
        public Item lastItem;
        public Tile lastInput;
        public float time, rot, angle;

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

                if(lastInput != null && lastItem != null){
                    int sa = sourceAngle();
                    int ta = targetAngle();

                    angle = ((sa == 0 && ta == 2)
                            || (sa == 2 && (ta == 0 || ta == 1))
                            || (sa == 1 && (ta == 0 || ta == 3))) ? -1 : 1;
                }

                if (target != null && items.total() > 0 && !Vars.state.isPaused()) {
                    rot += speed * angle * delta();
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

        public int sourceAngle() {
            for(int sourceAngle = 0; sourceAngle < 4; sourceAngle++) {
                if(nearby(sourceAngle) == lastInput.build) {
                    return sourceAngle;
                }
            }
            return -1;
        }

        public int targetAngle() {
            Building target = getTileTarget(lastItem, lastInput, false);
            for(int sourceAngle = 0; sourceAngle < 4; sourceAngle++) {
                if(nearby(sourceAngle) == target) {
                    return sourceAngle;
                }
            }
            return -1;
        }

        public void drawItem() {
            Building target = getTileTarget(lastItem, lastInput, false);
            if (lastInput != null && target != null  && lastInput.build != null) {
                boolean isf = lastInput.build.rotation == targetAngle();
                boolean alignment = targetAngle() == 0 || targetAngle() == 2;
                float ox, oy, s = size * 4, s2 = s * 2;

                if (alignment) {
                    if (isf) {
                        oy = (float) Math.sin(Math.PI * time) / 2.4f * s;
                        ox = (time * s2 - s) * (targetAngle() == 0 ? 1 : -1);
                    } else {
                        oy = sourceAngle() == 1 ? (time * -s + s) : (time * s - s);
                        ox = time * s * (targetAngle() == 0 ? 1 : -1);
                    }
                } else {
                    if (isf) {
                        ox = (float) Math.sin(Math.PI * time) / 2.4f * s;
                        oy = (time * s2 - s) * (targetAngle() == 1 ? 1 : -1);
                    } else {
                        ox = sourceAngle() == 0 ? (time * -s + s) : (time * s - s);
                        oy = time * s * (targetAngle() == 1 ? 1 : -1);
                    }
                }

                Draw.rect(lastItem.fullIcon, x + ox, y + oy, itemSize, itemSize);
            }
        }

        @Override
        public void draw() {
            super.draw();
            drawer.draw(this);
            if (lastItem != null && lastInput != null) {
                drawItem();
            }
            Draw.z(Layer.blockAdditive);
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