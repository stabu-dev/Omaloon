package omaloon.content;

import mindustry.content.*;
import mindustry.game.Objectives.*;

import static arc.struct.Seq.with;
import static mindustry.content.TechTree.*;
import static omaloon.content.OlItems.*;
import static omaloon.content.OlPlanets.glasmore;
import static omaloon.content.OlUnitTypes.*;
import static omaloon.content.blocks.OlCraftingBlocks.*;
import static omaloon.content.blocks.OlDefenceBlocks.*;
import static omaloon.content.blocks.OlDistributionBlocks.*;
import static omaloon.content.blocks.OlPowerBlocks.*;
import static omaloon.content.blocks.OlProductionBlocks.hammerDrill;
import static omaloon.content.blocks.OlStorageBlocks.*;

public class GlasmoreTechTree{
    public static TechNode root;

    public static void load(){
        root = glasmore.techTree = nodeRoot("omaloon-glasmore", landingCapsule, () -> {
            node(coreFloe, () -> {
                node(walker, () -> {
                    node(actionDroneMono);
                    node(attackDroneAlpha);
                });
            });

            node(tubeConveyor, with(new Research(hammerDrill)), () -> {
                node(tubeDistributor, with(new Research(hammerDrill)), () -> {
                    node(tubeJunction, () -> {
                        node(tubeSorter, () -> {
                            node(tubeOverflowGate, () -> node(tubeUnderflowGate, () -> {
                            }));
                        });
                        node(tubeBridge);
                    });
                });
            });

            node(liquidPump, () -> {
                node(liquidTube, () -> {
                    node(liquidJunction, () -> {
                        node(liquidOutlet);
                        node(liquidBridge);
                    });
                });
            });

            node(windTurbine, () -> {
                node(smallShelter, () -> {
                    node(repairer, with(new Research(coalGenerator)), () -> {

                    });
                });
                node(impulseNode, () -> {
                    node(coalGenerator, () -> {

                    });
                });
            });

            node(hammerDrill, () -> {
                node(compositePress, with(new Research(smallShelter)), () -> {
                    node(graphitePress, () -> {
                    });
                });
            });

//            node(apex, with(new OnSector(redeploymentPath)), () -> {
//                node(carborundumWall, () -> node(carborundumWallLarge));
//                node(blast, with(new SectorComplete(redeploymentPath)), () -> {
//                    node(convergence, with(new OnSector(deadValley)), () -> {
//
//                    });
//                });
//            });

//			node(legionnaire, () -> {
//				node(centurion, () -> {
//					node(praetorian);
//				});
//				node(cilantro, () -> {
//					node(basil, () -> {
//						node(sage);
//					});
//					node(effort);
//				});
//				node(lumen, () -> {
//					node(collector);
//				});
//			});

//            node(theCrater, () -> {
//                node(redeploymentPath, with(
//                new SectorComplete(theCrater),
//                new Research(coreFloe)
//                ), () -> {
//                    node(deadValley, with(new SectorComplete(redeploymentPath)), () -> {
//
//                    });
//                });
//            });

            nodeProduce(cobalt, () -> {
                nodeProduce(nickel, () -> {
                    nodeProduce(composite, () -> {

                    });
                });
                nodeProduce(Items.coal, () -> {
                    nodeProduce(Items.graphite, () -> {

                    });
                });
            });
        });
    }
}
