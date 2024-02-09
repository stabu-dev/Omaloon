package omaloon.content.blocks;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.BuildVisibility;
import omaloon.content.OlItems;
import omaloon.content.OlSounds;

import static mindustry.type.ItemStack.*;

public class OlDefenceBlocks {
    public static Block
            //turrets
            apex,
            //walls
            carborundumWall, carborundumWallLarge,

    end;

    public static void load(){
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
