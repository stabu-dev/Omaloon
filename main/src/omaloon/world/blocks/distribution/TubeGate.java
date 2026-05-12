package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

// TODO animation breaks if items flow too fast, unlikely to be fixed.
public class TubeGate extends TubeRouter{
    public boolean reverse = false;

    public TubeGate(String name){
        super(name);
        rotate = false;
    }

    public class TubeGateBuild extends TubeRouterBuild{
        public float globalRotation;

        @Override
        public boolean acceptItem(Building source, Item item){
            if(lastInput == null && items.any()) lastInput = source.tile;
            return team == source.team && lastItem == null && items.total() == 0;
        }

        // TODO blades snap into place, i'm not really sure how to fix that
        @Override
        public void drawRotator(float rotation){
            float r = Mathf.mod(lastItem == null ? globalRotation + 90f : (rotation + 90 + (lastInput != null ? lastInput.angleTo(this) : 0f)), 180);
            Draw.rect(rotatorRegion, x, y, r);

            Draw.alpha(Mathf.clamp(r / 90f - 1));
            Draw.rect(rotatorRegion, x, y, r + 180f);
            Draw.alpha(1f);
        }

        // TODO movement depends on lastInput, please do not
        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            if(item == null || from == null) return null;

            int otherDir = relativeTo(from);
            Building front = nearby((otherDir + 2) % 4);

            if(reverse){
                if(lookSides(item, from, false) == null && front != null && front.acceptItem(this, item)){
                    return front;
                }else{
                    return lookSides(item, from, set);
                }
            }else{
                if(front != null && front.acceptItem(this, item)){
                    return front;
                }else{
                    return lookSides(item, from, set);
                }
            }
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            time = 0f;
            lastInput = source.tile;
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

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                if (Angles.within(globalRotation, lastInput != null ? angleTo(lastInput) : 0f, 1f)){
                    lastItem = items.first();
                    time = 0f;
                    visualTarget = getPredictedTarget(lastItem);
                    visualTurn = turnTo(visualTarget != null ? relativeTo(visualTarget) : rotation);
                }else{
                    float dist = Angles.angleDist(globalRotation, lastInput != null ? angleTo(lastInput) : 0f);
                    if (dist > 90f && dist < 270f) globalRotation -= 180f;
                    globalRotation = Angles.moveToward(globalRotation, lastInput != null ? angleTo(lastInput) : 0f, 180f / speed * edelta());
                }
            }

            if(lastItem != null){
                time += 1f / speed * delta();
                Building target = getTileTarget(lastItem, lastInput, false);

                if(target != null && target != visualTarget){
                    float oldTurn = visualTurn;
                    visualTarget = target;
                    visualTurn = turnTo(relativeTo(visualTarget));

                    if (visualTurn == 2) {
                        time /= 2f;
                        visualTurn *= oldTurn;
                    }
                }

                if(target != null && (time >= 1f || instantTransfer)){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    globalRotation = visualTurn * 90f + (lastInput != null ? lastInput.angleTo(this) : 0f);
                    lastItem = null;
                    visualTarget = null;
                }
            }
        }
    }
}
