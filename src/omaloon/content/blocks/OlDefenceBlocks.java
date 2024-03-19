package omaloon.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlDefenceBlocks {
    public static Block
            //projectors
            mender, smallDeflector,
            //turrets
            apex,
            //walls
            carborundumWall, carborundumWallLarge,

    end;

    public static void load(){
        //region projectors
        mender = new MendProjector("mender"){{
            requirements(Category.effect, BuildVisibility.sandboxOnly, with());
            consumePower(0.2f);
            size = 1;
            reload = 190f;
            range = 34f;
            healPercent = 3.5f;
            health = 80;
        }};
        smallDeflector = new Deflector("small-deflector") {{
          requirements(Category.effect, BuildVisibility.sandboxOnly, with());
          size = 2;

          ambientSound = OlSounds.deflector;
          ambientSoundVolume = 0.08f;

          consumePower(0.2f);
          consume(new ConsumePressure(0.01f, true));
          consume(new PressureEfficiencyRange(30, 100f, 1f, false));
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

                        despawnEffect = Fx.hitBulletSmall;
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
