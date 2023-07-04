package ol.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TubeRouter extends Block {
    public float speed = 8f;
    public TextureRegion rotorRegion, bottomRegion;

    public TubeRouter(String name) {
        super(name);
        solid = false;
        underBullets = true;
        update = true;
        hasItems = true;
        itemCapacity = 1;
        acceptsItems = true;
        group = BlockGroup.transportation;
        unloadable = false;
        noUpdateDisabled = true;
    }

    public TextureRegion loadRegion(String prefix) {
        return Core.atlas.find(name + prefix);
    }

    @Override
    public void load() {
        super.load();
        rotorRegion = loadRegion("-rotor");
        bottomRegion = loadRegion("-bottom");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[]{bottomRegion, rotorRegion, region};
    }

    public class TubeRouterBuild extends Building {
        public Item lastItem;
        public Tile lastInput;
        public float time;
        public float rot = 0;

        @Override
        public void updateTile() {
            if (lastItem == null && items.any()) {
                lastItem = items.first();
            }

            if (lastItem != null) {
                time += 1f / speed * delta();
                Building target = getTileTarget(lastItem, lastInput, false);

                if (target != null && (time >= 1f || !(target.block instanceof TubeRouter || target.block.instantTransfer))) {
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    lastItem = null;
                }
            }

            if (!Vars.state.isPaused()) {
                if (items.total() > 0) {
                    rot += 7 * Time.delta;
                } else if (!(rot > 0)) {
                    rot = 0;
                }
            }
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if (lastItem != null) {
                drawItem();
            }
            Drawf.spinSprite(rotorRegion, x, y, rot % 360);
            Draw.rect(region, x, y);
        }

        public void drawItem() {
            float ox, oy;
            float s = size * 4;
            float s2 = s * 2;
            float func = (float) Math.sin(Math.PI * time) / 2.3f;

            if (rotation % 2 == 0) {
                oy = func * s;
                ox = (time * s2 - s) * (rotation == 0 ? 1 : -1);
            } else {
                ox = func * s;
                oy = (time * s2 - s) * (rotation == 1 ? 1 : -1);
            }

            Draw.rect(lastItem.fullIcon, x + ox, y + oy, itemSize, itemSize);
        }

        public Building getTileTarget(Item item, Tile from, boolean set) {
            int counter = rotation;
            for (int i = 0; i < proximity.size; i++) {
                Building other = proximity.get((i + counter) % proximity.size);
                if (set) rotation = ((byte) ((rotation + 1) % proximity.size));
                if (other.tile == from && from.block() == Blocks.overflowGate) continue;
                if (other.acceptItem(this, item)) {
                    return other;
                }
            }
            return null;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return items.get(item) < itemCapacity && proximity.contains(source) && item == lastItem;
        }

        @Override
        public void handleItem(Building source, Item item) {
            items.add(item, 1);
        }
    }
}
