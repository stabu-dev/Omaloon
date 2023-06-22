package ol.ai;

import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import ol.content.OlItems;
import ol.entity.MiningUnitEntity;
import ol.world.blocks.storage.MiningUnloadPoint.MiningUnloadPointBuild;

import static mindustry.Vars.indexer;

public class MiningUnitAI extends AIController {
    public Tile tmpTile;
    public Tile target;

    public void sync() {
        var en = entity();
        if(en.link != null) {
            en.link.synchronization();
            en.synchronization();
        }
    }

    public MiningUnloadPointBuild link() {
        return entity().link;
    }

    public boolean isMining() {
        return !entity().isEject();
    }

    public MiningUnitEntity entity() {
        return (MiningUnitEntity) unit;
    }

    public Building findUnloadBuilding(float range) {
        return indexer.findTile(unit.team, unit.x, unit.y, range, build -> {
            return build instanceof MiningUnloadPointBuild b && (b.link == null || b.link == entity());
        });
    }

    @Override
    public void updateMovement() {
        sync();
        var link = link();
        if(!entity().isBlock()) {
            var out = unit.tileOn().build;
            if(out instanceof MiningUnloadPointBuild && link == out) {
                entity().unloadTo();
            }

            try {
                var linkTile = link.tile;
                Tile moveTo = null;

                if(isMining() && target != null) {
                    moveTo = target;
                }

                if(moveTo == null) {
                    moveTo = linkTile;
                }

                moveTo(moveTo, 0);
                if(moveTo == target && unit.tileOn() == target) {
                    target.setNet(entity().getDrill(), unit.team, 0);
                    entity().instance = (Drill.DrillBuild) target.build;
                    //TODO spawn drill fx
                }
            } catch(Throwable ignored) {
                var core = unit.closestCore();
                if(core != null) {
                    moveTo(core, 40);
                }
                sync();
            }
        } else if(target.build != entity().instance) {
            entity().instance = null;
            unit.team.items().add(OlItems.grumon, 100);
            Call.unitDespawn(unit);
        } else if(entity().isFull()) {
            entity().eject();
            target = null;
            //TODO de spawn drill fx
        }

        /*
        if(!entity().isBlock()) {
            if(target != null) {
                if(isMining()) {
                    if(unit.tileOn() == target) {
>>>>>>> Stashed changes
                        target.setNet(entity().getDrill(), unit.team, 0);
                        entity().instance = (Drill.DrillBuild) target.build;
                    } else {
                        moveTo(target, 0);
                    }
                } else {
<<<<<<< Updated upstream
                    Building unloadBuilding = findUnloadBuilding(8);
                    if (unloadBuilding != null) {
                        entity().unloadTo((MiningUnloadPoint.MiningUnloadPointBuild) unloadBuilding);
                        if (!entity().isEject()) {
=======
                    Building out = out(8);
                    if(out != null) {
                        entity().unloadTo((MiningUnloadPointBuild) out);
                        if(!entity().isEject()) {
>>>>>>> Stashed changes
                            target = null;
                        }
                    } else {
                        moveTo(target, 4);
                    }
                }
            } else {
                Building core = unit.closestCore();
                if (core != null) {
                    moveTo(core, 40);
                }
            }
        } else if (target.build != entity().instance) {
            entity().instance = null;
        } else if (entity().isFull()) {
            entity().eject();
            target = null;
        }
         */
    }

    @Override
    public void updateTargeting() {
        if(link() == null) {
            entity().link = (MiningUnloadPointBuild) findUnloadBuilding(9000);
            if(link() != null) {
                link().link = entity();
                tmpTile = link().tile;
            }
            sync();
        } else if(link().link != entity() || tmpTile.build != link()) {
            tmpTile = null;
            link().link = null;
            entity().link = null;
            sync();
        }

        if(!entity().isBlock()) {
            var drill = entity().getDrill();
            if(isMining()) {
                if(target == null) {
                    sync();
                    if(link() != null) {
                        target = link().findOre(drill);
                    }
                } else if (target.build != null || !drill.canMine(target)) {
                    target = null;
                    sync();
                }
            }
        }
    }
}
