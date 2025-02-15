package omaloon.world.blocks.distribution;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;
import omaloon.annotations.*;

import static mindustry.Vars.player;

public class TubeGate extends OverflowGate{
    @Load("@-top-underflow")
    public TextureRegion topUnder;
    public Effect switchEffect = Fx.doorclose;
    public Sound switchSound = Sounds.door;

    public TubeGate(String name){
        super(name);
        saveConfig = copyConfig = true;
        config(Boolean.class, (TubeGateBuild build, Boolean invert) -> build.invert = invert);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config instanceof Boolean boll && boll) Draw.rect(topUnder, plan.drawx(), plan.drawy(), 0);
    }

    public class TubeGateBuild extends OverflowGateBuild{
        boolean invert = false;

        @Override
        public void tapped(){
            switchEffect.at(this, size);
            switchSound.at(this);
            configure(!invert);
        }

        @Override
        public Graphics.Cursor getCursor(){
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public void draw(){
            super.draw();
            if(invert){
                Draw.rect(topUnder, x, y, 0);
            }
        }

        @Override
        public Object config(){
            return invert;
        }

        @Override
        public @Nullable Building getTileTarget(Item item, Building src, boolean flip){
            int from = relativeToEdge(src.tile);
            if(from == -1) return null;
            Building to = nearby((from + 2) % 4);
            boolean
                fromInst = src.block.instantTransfer,
                canForward = to != null && to.team == team && !(fromInst && to.block.instantTransfer) && to.acceptItem(this, item),
                inv = invert == enabled;

            if(!canForward || inv){
                Building a = nearby(Mathf.mod(from - 1, 4));
                Building b = nearby(Mathf.mod(from + 1, 4));
                boolean ac = a != null && !(fromInst && a.block.instantTransfer) && a.team == team && a.acceptItem(this, item);
                boolean bc = b != null && !(fromInst && b.block.instantTransfer) && b.team == team && b.acceptItem(this, item);

                if(!ac && !bc){
                    return inv && canForward ? to : null;
                }

                if(ac && !bc){
                    to = a;
                }else if(bc && !ac){
                    to = b;
                }else{
                    to = (rotation & (1 << from)) == 0 ? a : b;
                    if(flip) rotation ^= (1 << from);
                }
            }

            return to;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            invert = read.bool();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(invert);
        }
    }
}
