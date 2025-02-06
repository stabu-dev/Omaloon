package omaloon.world.blocks.production;

import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.annotations.*;
import omaloon.content.*;

public class HammerDrill extends PressureDrill{
    public float shake = 0.3f;
    public float invertedTime = 200f;
    public Sound drillSound = OlSounds.hammer;
    public float drillSoundVolume = 0.2f, drillSoundPitchRand = 0.3f;
    @Load("@-hammer")
    public TextureRegion hammerRegion;

    public HammerDrill(String name){
        super(name);

        //does not drill in the traditional sense, so this is not even used
        hardnessDrillMultiplier = 0f;
        liquidBoostIntensity = 1f;
        ambientSound = Sounds.none;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        Draw.rect(hammerRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, hammerRegion};
    }

    @Override
    public float getDrillTime(Item item){
        return drillTime / drillMultipliers.get(item, 1f);
    }

    public class HammerDrillBuild extends PressureDrillBuild{
        public float smoothProgress = 0f;
        public float invertTime = 0f;

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            drawDefaultCracks();

            float fract = Mathf.clamp(smoothProgress, 0.25f, 0.3f);
            Draw.color(Pal.shadow, Pal.shadow.a);
            Draw.rect(hammerRegion, x - (fract - 0.25f) * 40, y - (fract - 0.25f) * 40, hammerRegion.width * fract, hammerRegion.width * fract);
            Draw.color();
            Draw.z(Layer.blockAdditive);
            Draw.rect(hammerRegion, x, y, hammerRegion.width * fract, hammerRegion.height * fract);
            if(dominantItem != null && drawMineItem){
                Draw.color(dominantItem.color);
                Draw.rect(itemRegion, x, y, itemRegion.width * fract, itemRegion.height * fract);
                Draw.color();
            }
        }

        @Override
        public boolean shouldConsume(){
            return items.total() <= itemCapacity - dominantItems && enabled;
        }

        @Override
        public void updateTile(){
            updatePressure();

            if(timer(timerDump, dumpTime)){
                dump(dominantItem != null && items.has(dominantItem) ? dominantItem : null);
            }

            if(dominantItem == null){
                return;
            }

            if(invertTime > 0f) invertTime -= delta() / invertedTime;

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);

            smoothProgress = Mathf.lerpDelta(smoothProgress, progress / (drillTime + 900f), 0.1f);

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0){
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;

                lastDrillSpeed = (speed * dominantItems * warmup) / delay;
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity){
                offload(dominantItem);
                consume();
                invertTime = 1f;
                progress %= delay;

                if(wasVisible){
                    Effect.shake(shake, shake, this);
                    drillSound.at(x, y, 1f + Mathf.range(drillSoundPitchRand), drillSoundVolume);
                    drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
                }
            }
        }
    }
}
