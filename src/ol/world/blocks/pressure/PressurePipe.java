package ol.world.blocks.pressure;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import mindustry.content.Blocks;
import mindustry.core.GameState;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.input.Placement;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.DirectionBridge;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidJunction;
import mindustry.world.meta.BlockGroup;
import ol.content.OlFx;
import ol.content.blocks.OlDistribution;
import ol.input.OLPlacement;
import ol.utils.Pressure;
import ol.utils.PressureAPI;
import ol.world.blocks.RegionAble;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class PressurePipe extends PressureBlock implements PressureReplaceable, RegionAble {
    public final ObjectMap<String, TextureRegion> cache = new ObjectMap<>();
    public @Nullable Block junctionReplacement, bridgeReplacement;

    /**draw connections?*/
    public boolean mapDraw = true;

    int timer = timers++;

    public void drawT(int x, int y, int rotation) {
        TextureRegion pressureIcon = Core.atlas.find("ol-arrow");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        if(rotation == 1 || rotation == 3) {
            Draw.rect(pressureIcon, dx, dy + ds, -90);
            Draw.rect(pressureIcon, dx, dy - ds, 90);
        }

        if(rotation == 0 || rotation == 2) {
            Draw.rect(pressureIcon, dx + ds, dy, 180);
            Draw.rect(pressureIcon, dx - ds, dy, 0);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawT(x, y, rotation);
    }

    public PressurePipe(String name) {
        super(name);

        conveyorPlacement = underBullets = rotate = update = solid = true;
        drawArrow = false;
        group = BlockGroup.power;
        priority = TargetPriority.transport;
    }

    @Override
    public boolean canReplace(Block other) {
        boolean valid = true;
        if(other instanceof PressurePipe pipe) {
            valid = PressureAPI.tierAble(pipe, tier);
        }

        return canBeReplaced(other) && valid;
    }

    @Override
    public void init(){
        super.init();

        if(junctionReplacement == null) {
            junctionReplacement = OlDistribution.pressureJunction;
        }

        //I did this because there will be no other pipes for pressure anyway.
        //If there is an idea that it is better to implement, implement it.
        if(bridgeReplacement == null || !(bridgeReplacement instanceof PressureBridge)) {
            bridgeReplacement = switch(tier) {
                case 2 -> OlDistribution.improvedPressureBridge;
                case 3 -> OlDistribution.reinforcedPressureBridge;
                default -> OlDistribution.pressureBridge;
            };
        }
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
        if(junctionReplacement == null) {
            return this;
        }

        Boolf<Point2> cont = p -> plans.contains(o -> {
            return o.x == req.x + p.x && o.y == req.y + p.y && o.rotation == req.rotation &&
                    (req.block instanceof PressurePipe || req.block instanceof PressureJunction);
        });

        return cont.get(Geometry.d4(req.rotation)) &&
                cont.get(Geometry.d4(req.rotation - 2)) &&
                req.tile() != null &&
                req.tile().block() instanceof PressurePipe &&
                Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans){
        if(bridgeReplacement == null) return;
        OLPlacement.calculateBridges(plans, (PressureBridge)bridgeReplacement);
    }

    //@Override
    //public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        /*Correct arrangement of regions is required*/
    //}

    @Override
    public String name() {
        return name;
    }

    public class PressurePipeBuild extends PressureBlockBuild {
        @Override
        public void updateTile() {
            super.updateTile();

            if(PressureAPI.overload(this)) {
                float random = Mathf.random(-3, 3);

                if(timer(PressurePipe.this.timer, Mathf.random(35, 65))) {
                    OlFx.pressureDamage.at(x + random/2, y + random/2, this.totalProgress() * random, Layer.blockUnder);
                }
            }
        }

        public boolean avalible(Building b) {
            return PressureAPI.netAble(this, b);
        }

        @Override
        public PressurePipeBuild self() {
            return this;
        }

        /**pipes name based on connections <name>-[L][R][T][B]

        example: if connected to all sides when loaded 1111
                if connected only right when loaded 0100
                ...
                if connected only right and left when loaded 1100...*/

        @Override
        public void draw() {
            if(mapDraw) {
                int tx = tileX();
                int ty = tileY();

                Building left   = world.build(tx - 1, ty);
                Building right  = world.build(tx + 1, ty);
                Building bottom = world.build(tx, ty - 1);
                Building top    = world.build(tx, ty + 1);

                boolean bLeft   = avalible(left)   || left    instanceof PressureJunction.PressureJunctionBuild;
                boolean bRight  = avalible(right)  || right   instanceof PressureJunction.PressureJunctionBuild;
                boolean bTop    = avalible(top)    || top     instanceof PressureJunction.PressureJunctionBuild;
                boolean bBottom = avalible(bottom) || bottom  instanceof PressureJunction.PressureJunctionBuild;

                int l = bLeft   ? 1 : 0;
                int r = bRight  ? 1 : 0;
                int t = bTop    ? 1 : 0;
                int b = bBottom ? 1 : 0;

                String sprite = "-" + l + "" + r + "" + t + "" + b;

                if(cache.get(sprite) == null) {
                    cache.put(sprite, loadRegion(sprite));
                }

                if(pressure > maxPressure && canExplode) {
                    if(state.is(GameState.State.paused)) {
                        Draw.rect(cache.get(sprite), this.x, this.y);
                    } else {
                        Draw.rect(cache.get(sprite), this.x, this.y, Mathf.random(-4, 4));
                    }
                } else {
                    Draw.rect(cache.get(sprite), this.x, this.y);
                }
            } else {
                super.draw();
            }

            this.drawTeamTop();
        }
    }
}