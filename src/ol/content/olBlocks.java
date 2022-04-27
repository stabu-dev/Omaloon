package ol.content;

import arc.util.Tmp;
import mindustry.content.*;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.entities.part.RegionPart;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import ol.graphics.olPal;
import ol.world.blocks.crafting.olCrafter;
import ol.world.blocks.defense.jointWall;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
//import ol.world.blocks.production.olBurstDrill;
import ol.world.blocks.defense.olWall;
import ol.world.blocks.power.olPanel;
import ol.world.draw.DrawImpact;

import static mindustry.type.ItemStack.with;

public class olBlocks{

	public static Block
	//Defence
	omaliteAlloyWall, omaliteAlloyWallLarge, testJoinWall,
	//Distribution
	//Drills
	//Power
	hyperReceiver,
	//Production
	lowTemperatureSmelter, fuser,
	//Turrets
	blueNight, zone,
	//Storages
	//Units
	//Ores&Environment
	oreOmalite;

	public static void load() {
		//endregion
		//region Defence
		omaliteAlloyWall = new olWall("omalite-alloy-wall") {{
			requirements(Category.defense, ItemStack.with(olItems.omaliteAlloy, 5, Items.titanium, 2));
			size = 1;
			statusDuration = 140f;
			health = 1420;
			insulated = true;
			status = StatusEffects.freezing;
			flashColor = olPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.003f;
		}};
		omaliteAlloyWallLarge = new olWall("omalite-alloy-wall-large") {{
			requirements(Category.defense, ItemStack.with(olItems.omaliteAlloy, 24, Items.titanium, 10));
			size = 2;
			statusDuration = 140f;
			health = 1840 * size * size;
			insulated = true;
			status = StatusEffects.freezing;
			flashColor = olPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.004f;
		}};

		testJoinWall = new jointWall("test-joint"){{
			requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(olItems.omaliteAlloy, 2));
			health = 999999999;
			size = 1;
		}};
		//endregion
		//region Production
		lowTemperatureSmelter = new olCrafter("low-temperature-smelter") {{
			size = 4;
			health = 540;
			requirements(Category.crafting, ItemStack.with(olItems.omalite, 80, Items.thorium, 80, Items.titanium, 100));
			craftTime = 270f;
			craftEffect = Fx.shieldBreak;
			updateEffectChance = 0.08f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.1f;
			accelerationSpeed = 0.0003f;
			decelerationSpeed = 0.006125f;
			powerProduction = 22f;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawImpact() {{
				    plasma1 = Items.titanium.color;
				    plasma2 = olPal.OLDarkBlue;
			    }});
			onCraft = tile -> {
				Tmp.v1.setToRandomDirection().setLength(27f / 3.4f);
				Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
				Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
			};
			consumePower(7);
			consumeItems(with(Items.titanium, 4, olItems.omalite, 2));
			consumeLiquid(olLiquids.liquidOmalite, 0.18f);
			outputItems = with(olItems.omaliteAlloy, 5);
			itemCapacity = 30;
		}};

		fuser = new GenericCrafter("fuser") {{
			requirements(Category.crafting, with(Items.surgeAlloy, 20, olItems.omalite, 50, Items.titanium, 80, Items.thorium, 65));
			craftTime = 185f;
			size = 3;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawLiquidTile(olLiquids.liquidOmalite){{
					drawLiquidLight = true;
				}},
				new DrawRegion("-rotator"){{
					spinSprite = true;
					rotateSpeed = 1f;
				}},
				new DrawDefault(),
				new DrawRegion("-top"));
			itemCapacity = 35;
			liquidCapacity = 45;
			hasPower = hasLiquids = hasItems = true;
			consumeLiquid(Liquids.water, 22f / 60f);
			consumeItems(new ItemStack(olItems.omalite, 2));
			outputLiquid = new LiquidStack(olLiquids.liquidOmalite,  22f / 60f);
			consumePower(2.4f);
		}};

		//endregion
		//region Turrets
		blueNight = new PowerTurret("blue-night") {{
			requirements(Category.turret, with(Items.copper, 20, Items.lead, 50, Items.graphite, 20, olItems.omaliteAlloy, 25, Items.silicon, 15));
			size = 3;
			range = 275f;
			recoil = 2f;
			health = 1980;
			inaccuracy = 3f;
			rotateSpeed = 5f;
			shootCone = 3f;
			shootSound = olSounds.olShot;
			ammoUseEffect = Fx.none;
			heatColor = olPal.OLDarkBlue;
			targetAir = false;
			shootEffect = olFx.blueShot;
			shootY = 10;
			drawer = new DrawTurret("intensified-") {{
				parts.add(new RegionPart("-mid") {{
					under = false;
				}});
				parts.add(new RegionPart("-side") {{
					mirror = true;
					under = true;
					moveX = 1f;
					moveY = -0.5f;
				}});
				parts.add(new RegionPart("-blade") {{
					heatProgress = PartProgress.heat;
					heatColor = olPal.OLDarkBlue;
					mirror = true;
					under = true;
					moveY = 0f;
					moveX = 1.5f;
					moveRot = 8;
				}});
			}};
			shootType = new BasicBulletType(9f, 240f) {{
				shrinkX = 0f;
				sprite = "ol-sphere";
				shrinkY = 0f;
				lifetime = 29f;
				status = StatusEffects.freezing;
				statusDuration = 120f;
				despawnEffect = hitEffect = new ExplosionEffect() {{
					waveColor = smokeColor = sparkColor = olPal.OLBlue;
					waveStroke = 4f;
					waveRad = 16f;
					waveLife = 15f;
					sparks = 5;
					sparkRad = 16f;
					sparkLen = 5f;
					sparkStroke = 4f;
				}};
				frontColor = olPal.OLBlue;
				backColor = olPal.OLBlue;
				width = height = 13f;
				collidesTiles = true;
				trailColor = olPal.OLBlue;
				trailWidth = 5f;
				trailLength = 9;
				trailEffect = Fx.trailFade;
				chargeEffect = olFx.blueSphere;
				splashDamage = 90f;
				splashDamageRadius = 24f;
			}};
			shoot.firstShotDelay = 55f;
			moveWhileCharging = false;
			chargeSound = olSounds.olCharge;
			reload = 120f;
			liquidCapacity = 40;
			consumePower(2f);
			consumeLiquid(olLiquids.liquidOmalite, 44.2f / 60f);
			smokeEffect = Fx.none;
			squareSprite = false;
		}};

		/*zone = new ItemTurret("zone"){{
			requirements(Category.turret, with(Items.copper, 25, Items.lead, 40, Items.graphite, 22, olItems.omaliteAlloy, 21, Items.silicon, 10));
			size = 3;
			health = 1880;
			rotateSpeed = 8f;
			range = 225f;
			targetAir = true;
			recoil = 3f;
			inaccuracy = 2f;
			shootCone = 1f;
			shootSound = olSounds.zoneShot;
			reload = 120f;
			itemCapacity = 32;
			squareSprite = false;
			drawer = new DrawTurret("intensified-");
			consumeCoolant(42f / 60f);
			ammo(
					olItems.omaliteAlloy
			);
		}};*/
		//endregion
		//region Ores
		oreOmalite = new OreBlock("omalite-ore"){{
			oreDefault = true;
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop = olItems.omalite;
			localizedName = itemDrop.localizedName;
			mapColor.set(itemDrop.color);
			useColor = true;
		}};
		//endregion
		//region Power
		hyperReceiver = new olPanel("hyper-receiver"){{
			requirements(Category.power, with(Items.titanium, 200, Items.surgeAlloy, 110, olItems.omaliteAlloy, 40));
			size = 4;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawLiquidTile(),
				new DrawDefault(),
			    new DrawRegion("-top")
			);
			hasLiquids = true;
			ambientSound = Sounds.none;
			powerProduction = 3.2f;
			liquidCapacity = 56;
			consumeLiquid(olLiquids.liquidOmalite, 12f / 920f);
		}};
		/*powerTower = new olPowerNode("power-tower"){{
        }};*/

//endregion
//region Drills
			/*explosiveDrill = new olBurstDrill("explosive-drill"){{
				requirements(Category.production, with(Items.copper, 85, Items.silicon, 80, Items.titanium, 70, Items.thorium, 95));
				drillTime = 60f * 12f;
				size = 4;
				hasPower = true;
				tier = 6;
				drillEffect = Fx.explosion;
				shake = 4f;
				itemCapacity = 40;
				//can't mine thorium for balance reasons, needs better drill
				researchCostMultiplier = 0.5f;
				consumes.power(160f / 60f);
				consumes.items(new ItemStack(Items.blastCompound, 1));
				consumes.liquid(Liquids.water, 0.2f);
			}};*/
//endregion
   }
}
