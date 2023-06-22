package ol.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.world.blocks.production.Drill;

public class OlDrill extends Drill {
    public TextureRegion teamRegion;
    public boolean isUnitDrill;

    public OlDrill(String name) {
        super(name);
        hasItems = true;
        acceptsItems = false;
        hasLiquids = false;
        liquidCapacity = 0f;
        outputsLiquid = false;
    }

    @Override
    public boolean isHidden() {
        return isUnitDrill;
    }

    @Override
    public boolean outputsItems() {
        return !isUnitDrill;
    }

    @Override
    public void load() {
        super.load();
        teamRegion = Core.atlas.find(name + "-team");
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class OlDrillBuild extends DrillBuild {
        @Override
        public void draw() {
            super.draw();
            if(teamRegion.found()) {
                Draw.color(team.color);
                Draw.rect(teamRegion, x, y, drawrot());
                Draw.reset();
            }
        }

        @Override
        public void updateTile() {
            if(dominantItem == null){
                return;
            }

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0){
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;

                lastDrillSpeed = (speed * dominantItems * warmup) / delay;
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;

                if(Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity) {
                items.add(dominantItem, 1);
                progress %= delay;

                if(wasVisible && Mathf.chanceDelta(updateEffectChance * warmup)) drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }
    }
}