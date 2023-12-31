package omaloon.content;

import mindustry.content.Fx;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import omaloon.content.blocks.*;
import omaloon.entities.bullet.FallingBulletType;
import omaloon.entities.bullet.HailStoneBulletType;

public class OlBlocks {
  //  public static Block test;
    public static void load(){
        OlEnvironmentBlocks.load();
        OlStorageBlocks.load();
        OlDistributionBlocks.load();
        OlPowerBlocks.load();

//        test = new PowerTurret("tests"){{
//            requirements(Category.defense, ItemStack.with(OlItems.cobalt, 1));
//            reload = 500f;
//            shoot.firstShotDelay = 10f;
//
//            shootType = new FallingBulletType("router"){{
//                fallTime = 200f;
//                fallingDamage = 100f;
//                fallingRadius = 10f;
//                hitEffect = Fx.blastExplosion;
//                despawnEffect = Fx.none;
//                scaleLife = true;
//                despawnHit = false;
//            }};
//
//            consumePower(10f);
//        }};

    }
}
