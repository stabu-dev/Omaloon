package omaloon.entities.bullet;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;

/* IS JUST POR HAILSTONE WEATHER */
public class HailStoneBulletType extends FallingBulletType {
    public TextureRegion[] variantsRegion;
    public int variants;
    public HailStoneBulletType(String sprite, int variants){
        super(sprite);
        this.variants = variants;
        this.lightRadius = 0;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.data = new HailStoneData(getRegion(b), fallTime);
    }

    @Override
    public void load() {
        variantsRegion = new TextureRegion[variants];

        for (int i = 0; i < variants; i++) {
            variantsRegion[i] = Core.atlas.find(sprite + "-" + i);
        }
    }

    @Override
    public void draw(Bullet b){
        Draw.alpha(255);
        Draw.z(Layer.flyingUnit);
        drawTrail(b);

        if (b.data instanceof HailStoneData data){
            drawFalling(b, data.region, regionColor);
        }
    }

    @Override
    public void despawned(Bullet b){
        if(despawnHit){
            hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor, b.data);
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    public TextureRegion getRegion(Bullet b){
        return variantsRegion[Mathf.floor(Mathf.randomSeed(b.id) * (variants - 1))];
    }

    public static class HailStoneData{
        public TextureRegion region;
        public float fallTime;

        public HailStoneData(TextureRegion region, float fallTime){
            this.region = region;
            this.fallTime = fallTime;
        }
    }
}
