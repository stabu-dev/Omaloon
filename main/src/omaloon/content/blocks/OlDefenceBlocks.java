package omaloon.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;
import omaloon.world.meta.*;

import static mindustry.type.ItemStack.with;

public class OlDefenceBlocks{
    public static Block
    //projectors
    smallShelter,
    //turrets
    apex, convergence
    ;

    public static void load(){
        smallShelter = new Shelter("small-shelter"){{
            requirements(Category.effect, with(OlItems.cobalt, 25, OlItems.nickel, 30));
            researchCostMultiplier = 0.3f;
            size = 2;
//            rechargeStandard = 2f;
//            shieldHealth = 260f;
            range = 120f;

            ambientSound = OlSounds.shelter;
            ambientSoundVolume = 0.08f;

            drawer = new DrawRegion("-base");

            consumePower(0.2f);
            consume(new ConsumeFluid(null, 5f / 60f){{
                continuous = true;
                hasOptimalPressure = true;

                startRange = 15f;
                endRange = 50f;
                efficiencyMultiplier = 2f;
                optimalPressure = 46.5f;

                curve = t -> Math.max(0f, Mathf.slope(t - 0.25f) * 2f - 1f);
            }});

            pressureConfig = new PressureConfig(){{
                acceptsPressure = true;
            }};
        }};
        //endregion
        //region turrets
        apex = new ItemTurret("apex"){{
            requirements(Category.turret, with(OlItems.composite, 10, OlItems.cobalt, 20));
            outlineColor = Color.valueOf("2f2f36");
            ammo(OlItems.cobalt,
            new BasicBulletType(3f, 9){{
                width = 7f;
                height = 9f;
                lifetime = 20.8f;
                ammoMultiplier = 3;

                shootEffect = Fx.shootSmallColor;
                smokeEffect = Fx.shootSmallSmoke;
                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;
                hitColor = OlItems.cobalt.color;

                trailWidth = 1.2f;
                trailLength = 10;
                trailColor = OlItems.cobalt.color;

                backColor = OlItems.cobalt.color;
                frontColor = Color.white;

                fragBullet = new BasicBulletType(2.5f, 2.5f){{
                    width = 4f;
                    height = 4f;
                    lifetime = 15f;

                    hitEffect = Fx.hitBulletColor;
                    despawnEffect = Fx.hitBulletColor;
                    hitColor = OlItems.cobalt.color;
                    backColor = OlItems.cobalt.color;

                    trailWidth = 0.8f;
                    trailLength = 5;
                    trailColor = OlItems.cobalt.color;
                }};

                fragOnHit = true;
                fragBullets = 4;
                fragRandomSpread = 45f;
                fragVelocityMin = 0.7f;
            }},
            Items.graphite, new BasicBulletType(4f, 16){{
                width = 7f;
                height = 10f;
                lifetime = 30f;
                ammoMultiplier = 2;
                reloadMultiplier = 0.9f;
                rangeChange = 20f;

                shootEffect = Fx.shootSmall;
                smokeEffect = Fx.shootSmallSmoke;
                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;
                hitColor = Items.graphite.color;

                trailWidth = 1.5f;
                trailLength = 8;
                trailColor = Items.graphite.color;

                backColor = Items.graphite.color;
                frontColor = Color.white;
                knockback = 0.8f;
            }}
            );

            shootY = 0f;

            shootSound = OlSounds.theShoot;

            drawer = new DrawTurret("gl-");

            reload = 30f;
            range = 100f;

            inaccuracy = 2f;
            rotateSpeed = 10f;
        }};

        convergence = new PowerTurret("convergence"){{
            requirements(Category.turret, with(OlItems.composite, 40, OlItems.cobalt, 15, Items.beryllium, 10));
            consumePower(0.2f);
            outlineColor = Color.valueOf("2f2f36");

            size = 1;
            range = 185f;
            shootCone = 45f;
            reload = 50f;
            targetGround = false;
            shootSound = OlSounds.thePowerShoot;

            drawer = new DrawTurret("gl-");

            shootType = new BasicBulletType(3f, 18f){{
                despawnEffect = hitEffect = Fx.hitSquaresColor;
                hitColor = Color.valueOf("8ca9e8");

                shootEffect = Fx.shootSmallColor;
                smokeEffect = Fx.none;

                lifetime = 60;
                collidesGround = false;
                collidesAir = true;

                shrinkX = shrinkY = width = height = 0f;
                height = 5;

                homingDelay = 1f;
                homingPower = 0.2f;
                homingRange = 120f;

                status = StatusEffects.shocked;
                statusDuration = 10f;

                backColor = Color.valueOf("8ca9e8");
                frontColor = Color.valueOf("d1efff");
                trailWidth = 1.8f;
                trailInterp = Interp.slope;
                trailLength = 8;
                trailColor = Color.valueOf("8ca9e8");
            }

            //I just didn't want to make a separate bulletType for one turret. (Maybe someday I will).
            @Override
            public void draw(Bullet b){
                super.draw(b);
                drawTrail(b);
                int sides = 4;
                float radius = 0f, radiusTo = 15f, stroke = 3f, innerScl = 0.5f, innerRadScl = 0.33f;
                Color color1 = Color.valueOf("8ca9e8"), color2 = Color.valueOf("d1efff");
                float progress = b.fslope();
                float rotation = 45f;
                float layer = Layer.effect;

                float z = Draw.z();
                Draw.z(layer);

                float rx = b.x, ry = b.y, rad = Mathf.lerp(radius, radiusTo, progress);

                Draw.color(color1);
                for(int j = 0; j < sides; j++){
                    Drawf.tri(rx, ry, stroke, rad, j * 360f / sides + rotation);
                }

                Draw.color(color2);
                for(int j = 0; j < sides; j++){
                    Drawf.tri(rx, ry, stroke * innerScl, rad * innerRadScl, j * 360f / sides + rotation);
                }

                Draw.color();
                Draw.z(z);
            }};
        }};
    }
}
