package omaloon.world.blocks.defense;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;

import static mindustry.Vars.*;

public class RepairProjector extends Block{
    static final float refreshInterval = 6f;

    public float range = 80f;
    public Color baseColor = Pal.heal.a(25);
    public float healWaveSpeed = 120f;
    public float reload = 20f;
    public float healAmount = 1f;
    public Sound mendSound = Sounds.healWave;
    public float mendSoundVolume = 0.5f;
    @Load("@-top")
    public TextureRegion top;

    public RepairProjector(String name){
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.repairSpeed, healAmount * 60f, StatUnit.perSecond);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    public class RepairTowerBuild extends Building implements Ranged{
        public float refresh = Mathf.random(refreshInterval);
        public float warmup = 0f;
        public float totalProgress = 0f;
        public float charge = Mathf.random(reload);
        public Seq<Building> buildingTargets = new Seq<>();
        public Seq<Unit> unitTargets = new Seq<>();

        @Override
        public void updateTile(){
            if(potentialEfficiency > 0 && (refresh += Time.delta) >= refreshInterval){
                unitTargets.clear();
                buildingTargets.clear();
                refresh = 0f;
                indexer.eachBlock(team, Tmp.r1.setCentered(x, y, range() * 2), b -> b.damaged() && !b.isHealSuppressed(), buildingTargets::add);
                Units.nearby(team, x, y, range(), u -> {
                    if(u.damaged()){
                        unitTargets.add(u);
                    }
                });
            }

            charge += delta();

            if(charge >= reload && efficiency > 0){
                charge = 0f;

                boolean any = false;

                for(var bTarget : buildingTargets){
                    if(bTarget.damaged()){
                        bTarget.heal(healAmount * efficiency);
                        bTarget.recentlyHealed();
                        Fx.healBlockFull.at(bTarget.x, bTarget.y, bTarget.block.size, baseColor, bTarget.block);
                        any = true;
                    }
                }
                for(var uTarget : unitTargets){
                    if(uTarget.damaged()){
                        uTarget.heal(healAmount * efficiency);
                        uTarget.healTime = 1f;
                        any = true;
                    }
                }

                if(any){
                    mendSound.at(this, 1f + Mathf.range(0.1f), mendSoundVolume);
                }
            }

            warmup = Mathf.lerpDelta(warmup, shouldConsume() ? efficiency : 0f, 0.08f);
            totalProgress += Time.delta / healWaveSpeed;
        }

        @Override
        public boolean shouldConsume(){
            return buildingTargets.size + unitTargets.size > 0;
        }

        @Override
        public void draw(){
            super.draw();
            if(warmup <= 0.001f) return;
            float f = 1f - (charge / reload);

            Draw.color(baseColor);
            Draw.alpha(warmup * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.rect(top, x, y);
            Draw.alpha(1f);
            Lines.stroke((2f * f * warmup));
            Lines.square(x, y, Math.min(1f + (1f - f) * size * tilesize / 2f, size * tilesize / 2f));
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public void drawSelect(){
            indexer.eachBlock(this, range, other -> true, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
            Units.nearby(team, x, y, range(), u -> {
                Draw.color(Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
                Lines.square(u.x, u.y, u.hitSize, 45f);
            });
            Drawf.dashCircle(x, y, range, Pal.placing);
        }
    }
}