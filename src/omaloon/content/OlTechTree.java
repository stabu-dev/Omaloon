package omaloon.content;

import mindustry.content.*;
import mindustry.game.Objectives.*;

import static arc.struct.Seq.*;
import static mindustry.content.TechTree.*;
import static omaloon.content.OlItems.*;
import static omaloon.content.OlSectorPresets.*;
import static omaloon.content.blocks.OlCraftingBlocks.*;
import static omaloon.content.blocks.OlDefenceBlocks.*;
import static omaloon.content.blocks.OlDistributionBlocks.*;
import static omaloon.content.blocks.OlPowerBlocks.*;
import static omaloon.content.blocks.OlProductionBlocks.*;
import static omaloon.content.blocks.OlStorageBlocks.*;

public class OlTechTree {
	public static void load() {
		OlPlanets.glasmore.techTree = nodeRoot("omaloon-glasmore", landingCapsule, () -> {
			node(coreFloe);

			node(tubeConveyor, with(new Research(hammerDrill)), () -> {
				node(tubeDistributor, with(new Research(hammerDrill)), () -> {
					node(tubeJunction, () -> {
						node(tubeSorter, with(new Produce(carborundum)), () -> {
							node(tubeGate, with(new Produce(carborundum)), () -> {

							});
						});
						node(tubeBridge);
					});
				});
			});

			node(hammerDrill, () -> {
				node(liquidPump, () -> {
					node(liquidTube, () -> {
						node(liquidJunction, () -> {
							node(liquidBridge);
						});
					});
					node(liquidGauge);
				});

				node(carborundumPress, with(new Research(smallShelter)), () -> {
					node(graphitePress, () -> {

					});
				});

				node(windTurbine, () -> {
					node(smallShelter, () -> {
						node(repairer, with(new Research(coalGenerator)), () -> {

						});
					});
					node(impulseNode, () -> {
						node(coalGenerator, with(new Produce(Items.graphite)), () -> {

						});
					});
				});
			});

			node(apex, with(new OnSector(redeploymentPath)), () -> {
				node(carborundumWall, () -> node(carborundumWallLarge));
				node(blast, with(new SectorComplete(redeploymentPath)), () -> {
					node(convergence, with(new OnSector(deadValley)), () -> {

					});
				});
			});

			// TODO change this when an unit factory is added
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

			node(theCrater, () -> {
				node(redeploymentPath, with(
					new SectorComplete(theCrater),
					new Research(coreFloe)
				), () -> {
					node(deadValley, with(new SectorComplete(redeploymentPath)), () -> {

					});
				});
			});

			nodeProduce(cobalt, () -> {
				nodeProduce(Items.beryllium, () -> {
					nodeProduce(carborundum, () -> {

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
