package ol.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.block.BlockAngles;
import me13.core.block.instance.AdvancedBlock;
import me13.core.flywheel.FlyDraw;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.meta.BlockGroup;

public class TubeRouter extends AdvancedBlock {
    public TextureRegion rotorRegion, bottomRegion;
    public float transportationSpeed = 0.05f;

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

        public boolean isValid() {
            var out = out();
            if(out != null && out != source) {
                var b = out.block;
                if(b instanceof Conveyor && BlockAngles.reverse(out.rotation) != index) {
                    return true;
                } else return !(b instanceof Conveyor) && out.acceptItem(this, item);
            }

            return false;
        }

        public void indexer() {
            index = (index + 1) % 4;

            if(isValid()) {
                timer = 0;
                if(index == 0 || index == 1) {
                    conf = -1;
                } else {
                    conf = 1;
                }
            } else {
                try {
                    indexer();
                } catch(StackOverflowError ignored) {
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
            indexer();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if(index == -1 || !isValid()) {
                indexer();
            } else if(item != null && source != null) {
                timer += transportationSpeed;
                if(timer >= 1) {
                    out().handleStack(item, 1, this);
                    removeStack(item, 1);
                    source = null;
                    item = null;
                    timer = 0;
                }
            }

            if(!Vars.state.isPaused()) {
                rot += 6 * conf;
            }
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            if(item != null && source != null) {
                boolean d2 = timer >= 0.5f;
                float ox, oy;

                if(d2) {
                    var ip = Geometry.d4(index);
                    float delta = (timer - 0.5f) / 0.5f;
                    ox = (size * 4f) * delta * ip.x;
                    oy = (size * 4f) * delta * ip.y;
                } else {
                    int sourceAngle;
                    for(sourceAngle = 0; sourceAngle < 4; sourceAngle++) {
                        if(nearby(sourceAngle) == source) {
                            break;
                        }
                    }
                    var ip = Geometry.d4(sourceAngle);
                    float delta = 1 - (timer / 0.5f);
                    ox = (size * 4f) * delta * ip.x;
                    oy = (size * 4f) * delta * ip.y;
                }

                Draw.rect(item.fullIcon, x + ox, y + oy, 6, 6);
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