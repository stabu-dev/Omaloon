package ol.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.*;
import arc.util.io.*;

import me13.core.block.*;
import me13.core.block.instance.*;

import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TubeRouter extends AdvancedBlock {
    public TextureRegion rotorRegion, bottomRegion;
    public float transportationSpeed = 0.08f;

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
    public void init() {
        super.init();
        if(size > 1) {
            throw new IllegalStateException("NO");
        }
    }

    @Override
    public void load() {
        super.load();
        rotorRegion = loadRegion("-rotor");
        bottomRegion = loadRegion("-bottom");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[] {bottomRegion, rotorRegion, region};
    }

    public class TubeRouterBuild extends AdvancedBuild {
        public Building source;
        public Item item;

        public float timer = 0;
        public float rot = 0;
        public int index = -1;
        public int conf = 1;

        public int sourceAngle() {
            for(int sourceAngle = 0; sourceAngle < 4; sourceAngle++) {
                if(nearby(sourceAngle) == source) {
                    return sourceAngle;
                }
            }

            return -1;
        }

        public float _func_239582() {
            return (float) Math.sin(Math.PI * timer) / 2.3f;
        }

        public void drawItem() {
            boolean isf = source.rotation == index;
            boolean alignment = index == 0 || index == 2;
            float ox, oy, s = size * 4, s2 = s * 2;

            if(alignment) {
                if(isf) {
                    oy = _func_239582() * s;
                    ox = (timer * s2 - s) * (index == 0 ? 1 : -1);
                } else {
                    oy = sourceAngle() == 1 ? (timer * -s + s) : (timer * s - s);
                    ox = timer * s * (index == 0 ? 1 : -1);
                }
            } else {
                if(isf) {
                    ox = _func_239582() * s;
                    oy = (timer * s2 - s) * (index == 1 ? 1 : -1);
                } else {
                    ox = sourceAngle() == 0 ? (timer * -s + s) : (timer * s - s);
                    oy = timer * s * (index == 1 ? 1 : -1);
                }
            }

            Draw.rect(item.fullIcon, x + ox, y + oy, itemSize, itemSize);
        }

        public boolean isValid() {
            var out = out();
            if (out != null && out != source && item != null) {
                var b = out.block;
                if(out.items.get(item) >= out.getMaximumAccepted(item)) {
                    return false;
                }

                if(b instanceof Conveyor && BlockAngles.reverse(out.rotation) != index) {
                    return true;
                } else {
                    return !(b instanceof Conveyor) && out.acceptItem(this, item);
                }
            }

            return false;
        }

        public void indexer() {
            index = Mathf.random(0, 4);

            if (isValid()) {
                timer = 0;

                int sa = sourceAngle();
                if(sa == 0 && index == 2) {
                    conf = 1;
                } else if(sa == 2) {
                    if(index == 0 || index == 1) {
                        conf = -1;
                    } else {
                        conf = 1;
                    }
                } else if(sa == 1) {
                    if(index == 0 || index == 3) {
                        conf = -1;
                    } else {
                        conf = 1;
                    }
                } else {
                    if(index == 0 || index == 1) {
                        conf = 1;
                    } else if(index == 2 || index == 3) {
                        conf = -1;
                    }
                }
            } else {
                try {
                    indexer();
                } catch (StackOverflowError ignored) {
                }
            }
        }

        public Building out() {
            return nearby(index);
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == this.item){
                this.item = null;
            }
            return result;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return team == source.team && items.total() == 0;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return 0;
        }

        @Override
        public void handleItem(Building source, Item item) {
            super.handleItem(source, item);
            this.source = source;
            this.item = item;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (index == -1 || !isValid()) {
                indexer();
            } else if (item != null && source != null) {
                timer += transportationSpeed * Time.delta;

                if (timer >= 1) {
                    out().handleItem(this, item);
                    removeStack(item, 1);
                    if(items.total() == 0) {
                        source = null;
                        item = null;
                        timer = 0;
                    }

                    indexer();
                }
            } else {
                if(item == null && items.total() > 0) {
                    content.items().forEach(it -> {
                        if(items.get(it) > 0) {
                            item = it;
                        }
                    });
                }
            }

            if (!Vars.state.isPaused()) {
                if (items.total() > 0) {
                    rot += 8 * conf * Time.delta;
                } else if (!(rot > 0)) {
                    rot = 0;
                }
            }
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if(item != null && source != null) {
                drawItem();
            }
            Drawf.spinSprite(rotorRegion, x, y, rot % 360);
            Draw.rect(region, x, y);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int sourcePos = read.i();
            source = sourcePos == -1 ? null : Vars.world.build(sourcePos);
            String itemName = read.str();
            item = itemName.equals("null") ? null : Vars.content.item(itemName);
            rot = read.f();
            timer = read.f();
            index = read.i();
            conf = read.i();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(source == null ? -1 : source.pos());
            write.str(item == null ? "null" : item.name);
            write.f(rot);
            write.f(timer);
            write.i(index);
            write.i(conf);
        }
    }
}