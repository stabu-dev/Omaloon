package ol.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import ol.graphics.OlPal;
import ol.graphics.OlShaders;
import ol.type.bullets.ControlledBulletType;
import ol.world.blocks.crafting.OlCrafter;
import ol.world.blocks.crafting.multicraft.IOEntry;
import ol.world.blocks.crafting.multicraft.MultiCrafter;
import ol.world.blocks.crafting.multicraft.Recipe;
import ol.world.blocks.defense.OlJoinWall;
import ol.world.blocks.defense.OlWall;
import ol.world.blocks.power.OlPanel;
import ol.world.draw.DrawCentryfuge;

import static mindustry.type.ItemStack.with;

public class OlBlocks{

	public static Block
		//Ores
		/*oreGrumon,*/ oreOmalite,
		//Environment
	        gravelDalanii, dalanii, deepDalanii,
	        //Defence
	        omaliteAlloyWall, omaliteAlloyWallLarge, testJoinWall,
	        //Distribution
	        //Drills
	        //Power
		hyperReceiver,
	        //Production
	        multiFactory, fuser, lowTemperatureSmelter,
	        //Turrets
	        blueNight, zone;
	        //Storages
	        //Units

	public static void load() {
		//region Ores
		oreOmalite = new OreBlock("omalite-ore"){{
			oreDefault = true;
			variants = 3;
			oreThreshold = 25.4F;
			oreScale = 0.3F;
			itemDrop = OlItems.omalite;
			localizedName = itemDrop.localizedName;
			mapColor.set(itemDrop.color);
			useColor = true;
		}};
		//endregion
		//region Environment
		gravelDalanii = new Floor("gravel-dalanii"){{
			itemDrop = Items.sand;
			playerUnmineable = true;
			speedMultiplier = 0.8f;
			variants = 3;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanii;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		dalanii = new Floor("flor-dalanii"){{
			speedMultiplier = 0.5f;
			variants = 0;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanii;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		deepDalanii = new Floor("deep-dalanii"){{
			speedMultiplier = 0.3f;
			variants = 0;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			drownTime = 210f;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanii;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		//endregion
		//region Defence
		omaliteAlloyWall = new OlWall("omalite-alloy-wall") {{
			requirements(Category.defense, ItemStack.with(OlItems.omaliteAlloy, 5, Items.titanium, 2));
			size = 1;
			health = 1420;
			insulated = true;
			status = StatusEffects.freezing;
			statusDuration = 140f;
			flashColor = OlPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.003f;
			drawDynamicLight = true;
			dynamicLightColor = OlPal.OLBlue;
			dynamicLightRadius = 10f;
			dynamicLightOpacity = 0.2f;
			canBurn = false;
			canApplyStatus = true;
		}};
		omaliteAlloyWallLarge = new OlWall("omalite-alloy-wall-large") {{
			requirements(Category.defense, ItemStack.with(OlItems.omaliteAlloy, 24, Items.titanium, 10));
			size = 2;
			health = 1840 * size * size;
			insulated = true;
			status = StatusEffects.freezing;
			statusDuration = 140f;
			flashColor = OlPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.004f;
			drawDynamicLight = true;
			dynamicLightColor = OlPal.OLBlue;
			dynamicLightRadius = 10f;
			dynamicLightOpacity = 0.2f;
			canBurn = false;
			canApplyStatus = true;
		}};

		testJoinWall = new OlJoinWall("test-joint"){{
				buildVisibility = BuildVisibility.sandboxOnly;
				category = Category.defense;
				health = 900;
				size = 1;

				damageLink = true;
			}

			public TextureRegion[] icons(){
				return new TextureRegion[]{Core.atlas.find(name, name)};
			}
		};
		//endregion
		//region Production
		multiFactory = new MultiCrafter("multi-factory"){{
			requirements(Category.crafting, ItemStack.with(OlItems.grumon, 12, Items.titanium, 11, Items.silicon, 5));
			size = 2;
			powerCapacity = 0;
			craftEffect = Fx.none;
			itemCapacity = 20;
			liquidCapacity = 20;
			health = 310;
			resolvedRecipes = Seq.with(
			new Recipe(
					new IOEntry(
					Seq.with(ItemStack.with(
							OlItems.grumon, 2,
							Items.silicon, 1
					)),
					Seq.with(),
					2f),

					new IOEntry(
					Seq.with(ItemStack.with(
							OlItems.magneticCombination, 1
					)),
			        Seq.with()),
			100f
			),
			new Recipe(
					new IOEntry(
					Seq.with(ItemStack.with(
							OlItems.omalite, 1,
							OlItems.grumon, 1
					)),
					Seq.with(LiquidStack.with(
							OlLiquids.dalanii, 12/60f
					)),
					1.2f),
					new IOEntry(
					Seq.with(ItemStack.with(
							OlItems.zarini, 2
					)),
					Seq.with(LiquidStack.with(
							Liquids.water, 12/60f
					))),
					160f
			),
			new Recipe(
					new IOEntry(
					Seq.with(ItemStack.with(
							OlItems.omalite, 1,
							Items.tungsten, 1
				    )),
					Seq.with(),
							0.7f),
					new IOEntry(
					Seq.with(ItemStack.with(
					OlItems.valkon, 1
				    )),
				    Seq.with()),
					140f
			));
		}};
		fuser = new GenericCrafter("fuser") {{
			requirements(Category.crafting, with(Items.surgeAlloy, 20, OlItems.omalite, 50, Items.titanium, 80, Items.thorium, 65));
			craftTime = 185f;
			size = 3;
			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawLiquidTile(Liquids.water),
					new DrawLiquidTile(OlLiquids.liquidOmalite){{
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
			consumeItems(new ItemStack(OlItems.omalite, 2));
			outputLiquid = new LiquidStack(OlLiquids.liquidOmalite,  22f / 60f);
			consumePower(2.4f);
		}};

		lowTemperatureSmelter = new OlCrafter("low-temperature-smelter") {{
			size = 4;
			health = 540;
			requirements(Category.crafting, ItemStack.with(OlItems.omalite, 80, Items.thorium, 80, Items.titanium, 100));
			craftTime = 270f;
			craftEffect = Fx.shieldBreak;
			updateEffectChance = 0.08f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.5f;
			accelerationSpeed = 0.0003f;
			decelerationSpeed = 0.006125f;
			powerProduction = 22f;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawCentryfuge(){{
				    plasma1 = Items.titanium.color;
				    plasma2 = OlPal.OLDarkBlue;
			    }});
			onCraft = tile -> {
				Tmp.v1.setToRandomDirection().setLength(27f / 3.4f);
				Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
				Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
			};
			consumePower(7);
			consumeItems(with(Items.titanium, 4, OlItems.omalite, 2));
			consumeLiquid(OlLiquids.liquidOmalite, 0.18f);
			outputItems = with(OlItems.omaliteAlloy, 5);
			itemCapacity = 30;
		}};

		//endregion
		//region Turrets
		blueNight = new PowerTurret("blue-night") {{
			requirements(Category.turret, with(Items.copper, 20, Items.lead, 50, Items.graphite, 20, OlItems.omaliteAlloy, 25, Items.silicon, 15));
			size = 3;
			range = 275f;
			recoil = 1f;
			health = 1980;
			inaccuracy = 1f;
			rotateSpeed = 3f;
			shootCone = 0.1f;
			shootSound = OlSounds.olShot;
			ammoUseEffect = Fx.none;
			heatColor = OlPal.OLDarkBlue;
			targetAir = false;
			shootEffect = OlFx.blueShot;
			shootY = 10;
			drawer = new DrawTurret("intensified-");
			shootType = new ControlledBulletType(9f, 240f) {{
				shrinkX = 0;
				sprite = "ol-sphere";
				shrinkY = 0;
				lifetime = 29f;
				status = StatusEffects.freezing;
				statusDuration = 120f;
				despawnEffect = hitEffect = new ExplosionEffect() {{
					waveColor = smokeColor = sparkColor = OlPal.OLBlue;
					waveStroke = 4f;
					waveRad = 16f;
					waveLife = 15f;
					sparks = 5;
					sparkRad = 16f;
					sparkLen = 5f;
					sparkStroke = 4f;
				}};
				frontColor = OlPal.OLBlue;
				backColor = OlPal.OLBlue;
				width = height = 13f;
				collidesTiles = true;
				trailColor = OlPal.OLBlue;
				trailWidth = 5f;
				trailLength = 9;
				trailEffect = Fx.railTrail;
				chargeEffect = OlFx.blueSphere;
				splashDamage = 95f;
				splashDamageRadius = 26f;
				homingPower = 0.4778f;
				homingRange = 275f;
				drag = 0.008f;
			}};
			shoot.firstShotDelay = 60f;
			moveWhileCharging = false;
			chargeSound = OlSounds.olCharge;
			reload = 140f;
			liquidCapacity = 40;
			consumePower(2f);
			consumeLiquid(OlLiquids.liquidOmalite, 44.2f / 60f);
			smokeEffect = Fx.none;
			squareSprite = false;
		}};

		/*zone = new ItemTurret("zone"){{
			requirements(Category.turret, with(Items.copper, 25, Items.lead, 40, Items.graphite, 22, OlItems.omaliteAlloy, 21, Items.silicon, 10));
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
					OlItems.omaliteAlloy
			);
		}};*/
		//endregion
		//region Power
		hyperReceiver = new OlPanel("hyper-receiver"){{
			requirements(Category.power, with(Items.titanium, 200, Items.surgeAlloy, 110, OlItems.omaliteAlloy, 40));
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
			consumeLiquid(OlLiquids.liquidOmalite, 12f / 920f);
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
