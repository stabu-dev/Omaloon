package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.Interp;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.entities.bullet.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;
import mindustry.content.Blocks.*;

import static mindustry.content.Blocks.duo;
import static mindustry.type.ItemStack.*;

public class OlDefenceBlocks {
    public static Block
            //projectors
            repairer, smallShelter,
            //turrets
            apex, convergence,
            //walls
            carborundumWall, carborundumWallLarge,

    end;

    public static void load(){
        //region projectors
        repairer = new RepairProjector("repairer"){{
            requirements(Category.effect, BuildVisibility.sandboxOnly, with());
            consumePower(0.2f);
            size = 1;
            range = 34f;
            healAmount = 1.6f;
            health = 80;
        }};
        smallShelter = new Shelter("small-shelter") {{
          requirements(Category.effect, BuildVisibility.sandboxOnly, with());
          size = 2;
          shieldRange = 170f;

          ambientSound = OlSounds.shelter;
          ambientSoundVolume = 0.08f;

          consumePower(0.2f);
          consume(new ConsumePressure(0.01f, true));
          consume(new PressureEfficiencyRange(15, 50f, 1.8f, false));
        }};
        //region turrets
        apex = new ItemTurret("apex"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with());
            outlineColor = Color.valueOf("2f2f36");
            ammo(
                    OlItems.cobalt,  new BasicBulletType(2.5f, 9){{
                        width = 7f;
                        height = 7f;
                        lifetime = 25f;
                        ammoMultiplier = 3;

                        despawnEffect = Fx.hitBulletColor;
                        hitEffect = Fx.none;
                        hitColor = OlItems.cobalt.color;

                        trailWidth = 1.3f;
                        trailLength = 10;
                        trailColor = OlItems.cobalt.color;

                        backColor = OlItems.cobalt.color;

                        fragBullet = new BasicBulletType(2.5f, 2.5f){{
                            width = 4f;
                            height = 4f;
                            lifetime = 15f;

                            despawnEffect = Fx.none;
                            hitEffect = Fx.none;
                            hitColor = OlItems.cobalt.color;

                            trailWidth = 0.8f;
                            trailLength = 10;
                            trailColor = OlItems.cobalt.color;

                            backColor = OlItems.cobalt.color;
                        }};

                        fragOnHit = true;
                        fragBullets = 4;
                        fragRandomSpread = 45f;
                        fragVelocityMin = 0.7f;
                    }}
            );

            shootY = 0f;

            shootSound = OlSounds.theShoot;

            drawer = new DrawTurret("gl-");

            reload = 30f;
            range = 100;

            inaccuracy = 2f;
            rotateSpeed = 10f;
        }};



        convergence = new PowerTurret("convergence"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with());
            consumePower(0.2f);
            outlineColor = Color.valueOf("2f2f36");

            size = 1;
            range = 134f;
            shootCone = 45f;
            reload = 80f;

            shootSound = OlSounds.convergence;

            shoot = new ShootSpread(7, 6);

            drawer = new DrawTurret("gl-");

            shootType = new AccelBulletType(1f, 20f){{
                velocityBegin = 2.8f;
                velocityIncrease = -2.15f;
                accelerateBegin = 0.1f;
                accelerateEnd = 0.7f;
                accelInterp = Interp.pow2Out;

                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;
                width = 52f;
                height = 5;
                shrinkY = -0.05f;
                shrinkX = 0.001f;
                hitSize = 9;
                knockback = 3.3f;

                lifetime = 100;
                collidesGround = false;
                collidesAir = true;
                impact = true;
                pierce = true;
                pierceCap = 2;

                backColor = Color.valueOf("d1efff");
                frontColor = Color.valueOf("8ca9e8");
                trailWidth = 2f;
                trailLength = 30;
                trailColor = Color.valueOf("8ca9e8");
            }};
        }};

        //walls
        int wallHealthMultiplier = 4;

        carborundumWall = new Wall("carborundum-wall"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, with());
            health = 90 * wallHealthMultiplier;
            researchCostMultiplier = 0.1f;
        }};

        carborundumWallLarge = new Wall("carborundum-wall-large"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, with());
            health = 90 * 4 * wallHealthMultiplier;
            size = 2;
            researchCostMultiplier = 0.1f;
        }};
    }
}
