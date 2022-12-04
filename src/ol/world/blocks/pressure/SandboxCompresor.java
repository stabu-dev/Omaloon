package ol.world.blocks.pressure;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.FloatSeq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.ui.Styles;
import mindustry.world.Tile;

//maxPressure in this block is max value in the config (!!!)
public class SandboxCompresor extends PressurePipe {
    public TextureRegion voidRegion;

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        rotation++;
        if(rotation > 3) {
            rotation -= 4;
        }

        drawT(x, y, rotation);
    }

    @Override
    public void drawBase(Tile tile) {
        if(tile.build != null){
            tile.build.draw();
        }else{
            Draw.rect(
                    variants == 0 ? region :
                            variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))],
                    tile.drawx(), tile.drawy());
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("pressure");
    }

    public SandboxCompresor(String name) {
        super(name);
        rotate = false;
        canExplode = false;
        copyConfig = true;

        configurable = true;

        config(Integer.class, (PressureSourceBuild b, Integer i) -> b.val = i);

        config(Integer.class, (PressureSourceBuild b, Integer i) -> b.val = i);

        config(String.class, (PressureSourceBuild b, String str) -> {
            try {
                b.val = Integer.parseInt(str);
            } catch(Exception ignored) {};
        });
    }

    @Override
    public void load() {
        super.load();
        voidRegion = loadRegion("-void");
    }

    public class PressureSourceBuild extends PressurePipeBuild {
        public int val = 0;

        @Override
        public void draw() {
            Draw.rect(voidMode() ? voidRegion : region, this.x, this.y, this.drawrot());
            this.drawTeamTop();
        }

        @Override
        public Integer config() {
            return val;
        }

        @Override
        public boolean isDanger() {
            return false;
        }

        public boolean voidMode() {
            return val == 0;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(val);
        }

        @Override
        public boolean storageOnly() {
            return false;
        }

        @Override
        public float pressureThread() {
            return enabled ? (voidMode() ? Integer.MIN_VALUE : val) : 0;
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            val = read.i();
        }

        @Override
        public void buildConfiguration(Table table) {
            table.pane(t -> {
                t.setBackground(Styles.black5);
                TextField f = t.field(Core.bundle.get("stat.pressure"), str -> {
                    try {
                        configure(Integer.parseInt(str));
                    } catch(Exception ignored) {}
                }).valid(val -> {
                    try {
                        Integer.parseInt(val);
                        return true;
                    } catch(Exception ignored) {
                        return false;
                    }
                }).pad(6f).get();
                f.setText(val + "");
                t.add(Core.bundle.get("stat.pressure")).pad(6f).growY();
            });
        }

        @Override
        public boolean inNet(Building b, PressureAble p, boolean j) {
            return true;
        }
    }
}