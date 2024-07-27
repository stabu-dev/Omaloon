package omaloon.content.blocks;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlDefenceBlocks {
    public static Block
            //projectors
            repairer, smallShelter,
            //turrets
            apex, convergence, blast, javelin,
            //walls
            carborundumWall, carborundumWallLarge,

    end;

    public static void load(){
        //region projectors
        repairer = new RepairProjector("repairer"){{
            requirements(Category.effect, with());
            consumePower(0.2f);
            size = 1;
            range = 34f;
            healAmount = 1.6f;
            health = 80;
        }};

        smallShelter = new Shelter("small-shelter") {{
          requirements(Category.effect, with(
            OlItems.cobalt, 25,
            Items.beryllium, 30
          ));
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
            requirements(Category.turret, with());
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
            requirements(Category.turret, with());
            consumePower(0.2f);
            outlineColor = Color.valueOf("2f2f36");

            size = 1;
            range = 185f;
            shootCone = 45f;
            reload = 50f;
            targetGround = false;
            shootSound = OlSounds.convergence;

            drawer = new DrawTurret("gl-");

            shootType = new BasicBulletType(2.5f, 18f, "omaloon-orb"){{
                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;

                lifetime = 73;
                collidesGround = false;
                collidesAir = true;

                shrinkX = shrinkY = 0f;
                height = 5;

                homingDelay = 1f;
                homingPower = 0.2f;
                homingRange = 120f;

                backColor = Color.valueOf("8ca9e8");
                frontColor = Color.valueOf("d1efff");
                trailWidth = 2.5f;
                trailLength = 4;
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

        blast = new BlastTower("blast"){{
            requirements(Category.turret, empty);
            size = 2;
            consumePower(70f / 60f);
            consume(new ConsumePressure(-6, false));
            consume(new PressureEfficiencyRange(-45f, -1f, 3f, true));
            targetGround = true;
            targetAir = false;
            damage = 0.6f;
            status = StatusEffects.slow;
            statusDuration = 30f;
            range = 70f;
            reload = chargeTime = 120f;
            shake = 3f;
        }};

        //TODO: either remove the turret or implement it in 0.2A
        /*
        javelin = new ConsumeTurret("javelin") {{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with());
            outlineColor = Color.valueOf("2f2f36");

            size = 2;

            reload = 270f;
            minRange = 64f;
            range = minRange + 96f * 4.6f;
            targetAir = targetUnderBlocks = minRangeShoot = false;

            drawer = new DrawTurret("gl-"){{
                parts.add(
                  new RegionPart("-missile"){{
                      y = 2f;
                      progress = PartProgress.smoothReload.curve(Interp.pow2In);

                      colorTo = new Color(1f, 1f, 1f, 0f);
                      color = Color.white;
                      mixColorTo = Pal.accent;
                      mixColor = new Color(1f, 1f, 1f, 0f);
                      outline = false;
                      under = true;

                      layerOffset = -0.01f;
                  }}
                );
            }};

            shootSound = OlSounds.theShoot;
            consumeItem(Items.coal, 1);
            shootType = new BasicBulletType(1.6f, 12f, "omaloon-javelin-missile-outlined"){{
                lifetime = 40f;
                ammoMultiplier = 1f;
                collides = collidesAir = collidesGround = false;

                shrinkX = shrinkY = 0f;
                width = 6.5f;
                height = 11.5f;

                shootEffect = OlFx.javelinShoot;
                despawnEffect = OlFx.javelinMissileShoot;
                hitEffect = Fx.none;
                despawnSound = Sounds.missileLarge;

                layer = Layer.turret - 0.01f;
                despawnUnit = new MissileUnitType("javelin-missile") {{
                    hittable = drawCell = false;
                    speed = 4.6f;
                    maxRange = 6f;
                    lifetime = 60f * 1.6f;
                    outlineColor = Color.valueOf("2f2f36");
                    engineColor = trailColor = Pal.redLight;
                    engineLayer = Layer.effect;
                    engineSize = 1.3f;
                    engineOffset = 5f;
                    rotateSpeed = 0.25f;
                    trailLength = 18;
                    trailWidth = 0.5f;
                    missileAccelTime = 0f;
                    lowAltitude = true;
                    loopSound = Sounds.missileTrail;
                    loopSoundVolume = 0.6f;
                    deathSound = Sounds.largeExplosion;
                    targetAir = false;

                    health = 210;

                    weapons.add(new Weapon(){{
                          shootCone = 360f;
                          mirror = false;
                          reload = 1f;
                          deathExplosionEffect = Fx.massiveExplosion;
                          shootOnDeath = true;
                          shake = 10f;
                          bullet = new ExplosionBulletType(700f, 65f){{
                              hitColor = Pal.redLight;

                              collidesAir = false;
                              buildingDamageMultiplier = 0.3f;

                              ammoMultiplier = 1f;
                              fragLifeMin = 0.1f;
                              fragBullets = 7;
                              fragBullet = new ArtilleryBulletType(3.4f, 32){{
                                  buildingDamageMultiplier = 0.3f;
                                  drag = 0.02f;
                                  hitEffect = Fx.massiveExplosion;
                                  despawnEffect = Fx.scatheSlash;
                                  knockback = 0.8f;
                                  lifetime = 23f;
                                  width = height = 18f;
                                  collidesTiles = false;
                                  splashDamageRadius = 40f;
                                  splashDamage = 80f;
                                  backColor = trailColor = hitColor = Pal.redLight;
                                  frontColor = Color.white;
                                  smokeEffect = Fx.shootBigSmoke2;
                                  despawnShake = 7f;
                                  lightRadius = 30f;
                                  lightColor = Pal.redLight;
                                  lightOpacity = 0.5f;

                                  trailLength = 10;
                                  trailWidth = 0.5f;
                                  trailEffect = Fx.none;
                              }};
                          }};
                      }});
                }};
            }};
        }};
        */

        //walls
        int wallHealthMultiplier = 4;

        carborundumWall = new Wall("carborundum-wall"){{
            requirements(Category.defense, with());
            health = 90 * wallHealthMultiplier;
            researchCostMultiplier = 0.1f;
        }};

        carborundumWallLarge = new Wall("carborundum-wall-large"){{
            requirements(Category.defense, with());
            health = 90 * 4 * wallHealthMultiplier;
            size = 2;
            researchCostMultiplier = 0.1f;
        }};
    }
}
