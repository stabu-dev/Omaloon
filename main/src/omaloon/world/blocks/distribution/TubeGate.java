package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

//TODO: fix main output to fallback output animation switch being a bit broken
public class TubeGate extends TubeRouter{
    public boolean reverse = false;

    public TubeGate(String name){
        super(name);
        rotate = false;
    }

    public class TubeGateBuild extends TubeRouterBuild{
        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && getTileTarget(item, source.tile, false) != null;
        }

        @Override
        public Building getPredictedTarget(Item item){
            return getTileTarget(item, lastInput, false);
        }

        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            if(item == null || from == null) return null;
            int rel = relativeTo(from);
            if(rel == -1) return null;
            int forward = (rel + 2) % 4;
            Building straight = nearby(forward);
            if(!reverse && canTarget(straight, item)) return straight;
            int s1 = (rel + 1) % 4, s2 = (rel + 3) % 4;
            Building b1 = nearby(s1), b2 = nearby(s2);
            boolean c1 = canTarget(b1, item), c2 = canTarget(b2, item);
            if(c1 && c2){
                int target = (rotation & (1 << rel)) == 0 ? s1 : s2;
                if(set) rotation ^= (1 << rel);
                return nearby(target);
            }
            if(c1) return b1;
            if(c2) return b2;
            return reverse && canTarget(straight, item) ? straight : null;
        }

        protected boolean canTarget(@Nullable Building other, Item item){
            if(other == null || other.team != team) return false;
            if(other.acceptItem(this, item)) return true;
            return relativeTo(other) >= 0 && Time.time - lastFlow[relativeTo(other)] <= 15f;
        }

        @Override
        public float turnTo(int direction){
            if(lastItem == null) return visualTurn;
            return super.turnTo(direction);
        }

        @Override
        public void drawRotator(float rotation){
            float t = (lastItem == null ? 1f : Mathf.clamp(time));
            float relIn = (lastInput == null ? 0 : relativeTo(lastInput));
            float b = 135f - 90f * Mathf.clamp(1f - Math.abs(visualTurn - 1f));
            float r = relIn * 90f + b + visualTurn * 90f * t;
            float a = Draw.getColorAlpha();
            r = Mathf.mod(r, 180f);
            Draw.rect(rotatorRegion, x, y, r);
            Draw.alpha(r / 180f * a);
            Draw.rect(rotatorRegion, x, y, r - 180f);
            Draw.alpha(a);
        }
    }
}
