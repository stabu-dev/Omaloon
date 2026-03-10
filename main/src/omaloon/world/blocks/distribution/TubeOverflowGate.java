package omaloon.world.blocks.distribution;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import arc.util.*;
import arc.graphics.g2d.*;
import mindustry.graphics.*;

import static mindustry.Vars.itemSize;

public class TubeOverflowGate extends TubeRouter {
    public TubeOverflowGate(String name){
        super(name);
        rotate = false;
    }

    public class TubeOverflowGateBuild extends TubeRouterBuild {

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);
            Draw.rect(bottomRegion, x, y);

            Draw.z(Layer.block - 0.1f);
            Building target = getTileTarget(lastItem, lastInput, false);
            float rot = 0f;

            if(target != null && lastItem != null && lastInput != null){
                int turn = arc.math.Mathf.mod(relativeTo(target) + 1 - relativeTo(lastInput), 4) - 1;
                rot = turn * 90f * arc.math.Mathf.clamp(time);
                float d = itemInterp.apply(arc.math.Mathf.clamp(time));

                Draw.rect(
                    lastItem.uiIcon,
                    x + arc.math.Angles.trnsx(rot + relativeTo(lastInput) * 90f, 4f * d),
                    y + arc.math.Angles.trnsy(rot + relativeTo(lastInput) * 90f, 4f * d),
                    itemSize,
                    itemSize
                );
            }

            Draw.z(Layer.block);

            float rotatorAngle = 45f;
            if(target != null && lastItem != null && lastInput != null){
                int turn = arc.math.Mathf.mod(relativeTo(target) + 1 - relativeTo(lastInput), 4) - 1;
                rotatorAngle = rot + relativeTo(lastInput) * 90f + (turn > 0 ? -45f : 45f);
            }

            Drawf.spinSprite(rotatorRegion, x, y, rotatorAngle);
            Draw.rect(region, x, y);
            if(sideRegion[0].found()) Draw.rect(sideRegion[0], x, y, rotdeg());
        }

        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            if(item == null || from == null) return null;

            int otherDir = relativeTo(from);
            Building front = nearby((otherDir + 2) % 4);

            if(front != null && front.acceptItem(this, item)){
                return front;
            }else{
                return lookSides(item, from, set);
            }
        }

        public @Nullable Building lookSides(Item item, Tile from, boolean set){
            int otherDir = relativeTo(from);
            for(int i = 0; i < 4; i += 2){
                Building other = nearby((otherDir + 1 + i + targetRot) % 4);
                if(set) targetRot = (byte)((i + targetRot + 2) % 4);
                if(other == null || other.tile == from) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }
            return null;
        }
    }
}
