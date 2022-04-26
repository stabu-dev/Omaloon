package ol.content;

import arc.util.Tmp;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.TreeBlock;
import mindustry.world.draw.DrawRotator;
import mma.world.draw.MultiDrawBlock;
import ol.world.blocks.crafting.olCrafter;
import ol.world.blocks.defense.olColdWall;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.power.SolarGenerator;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawMixer;
import mindustry.world.draw.DrawSmelter;
import ol.world.draw.DrawImpact;

import static mindustry.type.ItemStack.with;

public class olBlocks implements ContentList {

	public static Block
	//Defence
	omaliteAlloyWall, omaliteAlloyWallLarge,
	//Distribution
	//Drills
	//percussionTower,
	//Power
	hyperReceiver, //powerTower,
	//Production
	lowTemperatureSmelter, fuser,
	//Turrets
	bluenight,
	//Storages
	//Units
	//Ores
	oreOmalite;

        @Override
	public void load() {
		//endregion
		//region Defence
		omaliteAlloyWall = new olColdWall("omalite-alloy-wall"){{
			requirements(Category.defense, ItemStack.with(olItems.omaliteAlloy, 5,Items.titanium, 10));
			size = 1;
			health = 920 * size * size;
			insulated = true;
		}};
		omaliteAlloyWallLarge = new olColdWall("omalite-alloy-wall-large"){{
			requirements(Category.defense, ItemStack.with(olItems.omaliteAlloy, 50,Items.titanium, 110));
			size = 2;
			health = 1840 * size * size;
			insulated = true;
		}};
		//endregion
		//region Production
		lowTemperatureSmelter = new olCrafter("low-temperature-smelter"){{
			size = 4;
			health = 540;
			requirements(Category.crafting, ItemStack.with(olItems.omalite, 80,Items.thorium, 80,Items.titanium, 100));
			craftTime = 350f;
			craftEffect = Fx.shieldBreak;
			updateEffectChance = 0.08f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.4f;
			warmupSpeed = 0.0008f;
			deWarmupSpeed = 0.006125f;
			powerProduction = 12f;
			drawer = new DrawImpact(){{
				plasma1 = Items.titanium.color;
				plasma2 = olItems.omalite.color;
			}};
			onCraft = tile -> {
				Tmp.v1.setToRandomDirection().setLength(28f / 4f);
				Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
				Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
			};
			consumes.power(7);
			consumes.items(with(Items.titanium, 4, olItems.omalite, 2));
			consumes.liquid(olLiquids.liquidOmalite, 0.18f);
			outputItems = with(olItems.omaliteAlloy, 4);
			itemCapacity = 30;
		}};

		fuser = new GenericCrafter("fuser") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 20, olItems.omalite, 50, Items.titanium, 80, Items.thorium, 65));
				craftTime = 85;
				size = 3;
				drawer = new MultiDrawBlock(
						new DrawMixer(true),
						new DrawRotator()
				);
				itemCapacity = 30;
				liquidCapacity  = 20;
				hasPower = hasLiquids = hasItems = true;
				consumes.liquid(Liquids.water,0.2f);
				consumes.items(new ItemStack(olItems.omalite, 2));
				outputLiquid = new LiquidStack(olLiquids.liquidOmalite, 12f);
				consumes.power(4f);
			}
		};

		//endregion
		//region Turrets
			/*bluenight = new PowerTurret("blue-night"){{
			    requirements(Category.turret, with(Items.copper, 20, Items.lead, 50, Items.graphite, 20, olItems.omaliteAlloy, 25, Items.silicon, 15));
				size = 3;
				range = 175f;
				recoilAmount = 2f;
				inaccuracy = 3f;
				rotateSpeed = 6f;
				shootCone = 3f;
				shootSound = Sounds.laser;
				ammoUseEffect = Fx.casing1;
				targetAir = true;
				shootType = olBullets.blueSphere;
				chargeTime = 30f;
				chargeMaxDelay = 30f;
				chargeSound = Sounds.lasercharge2;
				powerUse = 3f;
				chargeEffect = olFx.blueSphere;
				smokeEffect = Fx.none;
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
		hyperReceiver = new SolarGenerator("hyper-receiver"){{
			requirements(Category.power, with(Items.titanium, 200, Items.surgeAlloy, 110, olItems.omaliteAlloy, 40));
			size = 4;
			powerProduction = 3f;
		}};
		/*powerTower = new olPowerNode("power-tower"){{
        }};*/

//endregion
//region Drills
		/*percussionTower = new PercussionTower("percussion-tower"){{
		}};*/
//endregion
   }
}
