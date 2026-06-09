package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

//TODO: fix the rotation reversal at maximum throughput to a single output
public class TubeGate extends TubeRouter{
    public boolean reverse = false;

    public TubeGate(String name){
        super(name);
        rotate = false;
    }

    public class TubeGateBuild extends TubeRouterBuild{
        public float rotorStart = -1f, lastEnd = -1f;
        public @Nullable Item prevItem = null;

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && getTileTarget(item, source.tile, false) != null;
        }

        @Override
        public Building getPredictedTarget(Item item){
            return getVisualTarget(item, lastInput);
        }

        @Override
        protected @Nullable Building getVisualTarget(Item item, Tile from){
            Building predicted = getTileTarget(item, from, false);
            Building immediate = getTileTarget(item, from, false, false);

            if(predicted == null || predicted == immediate) return predicted;
            if(immediate == null) return predicted;
            return shouldKeepFlowPrediction(predicted, item) ? predicted : immediate;
        }

        @Override
        protected @Nullable Building getSendTarget(Item item, Tile from){
            return getTileTarget(item, from, false);
        }

        @Override
        protected void setupItem(Item item){
            prevItem = null;
            super.setupItem(item);
        }

        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            return getTileTarget(item, from, set, true);
        }

        public Building getTileTarget(Item item, Tile from, boolean set, boolean allowFlowPrediction){
            if(item == null || from == null) return null;
            int rel = relativeTo(from);
            if(rel == -1) return null;
            int forward = (rel + 2) % 4;
            Building straight = nearby(forward);
            if(!reverse && canTarget(straight, item, allowFlowPrediction)) return straight;
            int s1 = (rel + 1) % 4, s2 = (rel + 3) % 4;
            Building b1 = nearby(s1), b2 = nearby(s2);
            boolean c1 = canTarget(b1, item, allowFlowPrediction), c2 = canTarget(b2, item, allowFlowPrediction);
            if(c1 && c2){
                int target = (rotation & (1 << rel)) == 0 ? s1 : s2;
                if(set) rotation ^= (1 << rel);
                return nearby(target);
            }
            if(c1) return b1;
            if(c2) return b2;
            return reverse && canTarget(straight, item, allowFlowPrediction) ? straight : null;
        }

        protected boolean canTarget(@Nullable Building other, Item item, boolean allowFlowPrediction){
            if(other == null || other.team != team) return false;
            if(other.acceptItem(this, item)) return true;
            return allowFlowPrediction && relativeTo(other) >= 0 && Time.time - lastFlow[relativeTo(other)] <= 15f;
        }

        protected boolean shouldKeepFlowPrediction(Building other, Item item){
            int rel = relativeTo(other);
            if(rel < 0 || Time.time - lastFlow[rel] > 15f) return false;

            return other.items == null || other.items.get(item) < other.getMaximumAccepted(item);
        }

        @Override
        public float turnTo(int direction){
            if(lastItem == null) return visualTurn;
            return super.turnTo(direction);
        }

        @Override
        public void drawRotator(float rotation){
            if(lastItem != prevItem){
                prevItem = lastItem;
                if(lastItem != null){
                    float relIn = (lastInput == null ? 0 : relativeTo(lastInput));
                    float b = 135f - 90f * Mathf.clamp(1f - Math.abs(visualTurn - 1f));
                    float targetStart = relIn * 90f + b;

                    rotorStart = (lastEnd == -1f) ? targetStart
                    : targetStart - 90f + Mathf.mod(lastEnd - targetStart + 90f, 180f);
                }
            }

            if(lastItem != null){
                lastEnd = rotorStart + visualTurn * 90f;
            }else if(lastEnd != -1f){
                lastEnd = Math.round((lastEnd - 45f) / 90f) * 90f + 45f;
            }

            float r = (lastItem == null)
            ? ((lastEnd == -1f) ? (lastInput == null ? 0 : relativeTo(lastInput)) * 90f + 135f : lastEnd)
            : Mathf.lerp(rotorStart, rotorStart + visualTurn * 90f, Mathf.clamp(time));
            drawRotorAt(r);
        }

        private void drawRotorAt(float r){
            float a = Draw.getColorAlpha();
            r = Mathf.mod(r, 180f);
            Draw.rect(rotatorRegion, x, y, r);
            Draw.alpha(r / 180f * a);
            Draw.rect(rotatorRegion, x, y, r - 180f);
            Draw.alpha(a);
        }
    }
}