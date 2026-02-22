package omaloon.entities.comp;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import omaloon.annotations.Annotations.*;

import static mindustry.Vars.*;

@EntityComponent
abstract class MockBuilderComp implements Unitc, Builderc {
    @Import float buildSpeedMultiplier;
    @Import Queue<BuildPlan> plans;
    @Import UnitType type;
    @Import Team team;

    @Import BuildPlan lastActive;
    @Import float buildAlpha;
    @Import float buildCounter;
    @Import boolean updateBuilding;
    @Import int lastSize;

    @Replace
    @Override
    public void drawBuilding() {

    }

    @Replace
    @Override
    public void updateBuildLogic() {
        if (type.buildSpeed <= 0.0F) return;
        if (!headless) {
            if (lastActive != null && buildAlpha <= 0.01F) {
                lastActive = null;
            }
            buildAlpha = Mathf.lerpDelta(buildAlpha, activelyBuilding() ? 1.0F : 0.0F, 0.15F);
        }
        validatePlans();
        if (!updateBuilding || !canBuild()) {
            return;
        }
        float finalPlaceDst = state.rules.infiniteResources ? Float.MAX_VALUE : type.buildRange;
        boolean infinite = state.rules.infiniteResources || team().rules().infiniteResources;
        buildCounter += Time.delta;
        if (Float.isNaN(buildCounter) || Float.isInfinite(buildCounter)) buildCounter = 0.0F;
        buildCounter = Math.min(buildCounter, 10.0F);
        boolean instant = state.rules.instantBuild && state.rules.infiniteResources;
        int maxPerFrame = instant ? plans.size : 10;
        int count = 0;
        var core = core();
        if ((core == null && !infinite)) return;
        while ((buildCounter >= 1 || instant) && count++ < maxPerFrame && plans.size > 0) {
            buildCounter -= 1.0F;
            if (plans.size > 1) {
                int total = 0;
                int size = plans.size;
                float bestDst = Float.MAX_VALUE;
                boolean foundAny = false;
                int bestIndex = -1;
                while (total < size) {
                    var plan = buildPlan();
                    float dst = plan.dst2(self());
                    boolean within = dst <= finalPlaceDst * finalPlaceDst;
                    if (within && !shouldSkip(plan, core)) {
                        foundAny = true;
                        break;
                    } else if (within && dst < bestDst) {
                        bestIndex = total;
                        bestDst = dst;
                    }
                    plans.removeFirst();
                    plans.addLast(plan);
                    total++;
                }
                if (!foundAny && bestIndex > 0 && !within(buildPlan(), finalPlaceDst)) {
                    for (int i = 0; i < bestIndex; i++) {
                        plans.addLast(plans.removeFirst());
                    }
                }
            }
            BuildPlan current = buildPlan();
            Tile tile = current.tile();
            lastActive = current;
            buildAlpha = 1.0F;
            if (current.breaking) lastSize = tile.block().size;
            if (!within(tile, finalPlaceDst)) continue;
            if (!headless) {
                Vars.control.sound.loop(Sounds.loopBuild, tile, 1.3F);
            }
            if (!(tile.build instanceof ConstructBlock.ConstructBuild cb)) {
                if (!current.initialized && !current.breaking && Build.validPlaceIgnoreUnits(current.block, team, current.x, current.y, current.rotation, true, true)) {
                    if (Build.checkNoUnitOverlap(current.block, current.x, current.y)) {
                        boolean hasAll = infinite || current.isRotation(team) || (tile.team() == Team.derelict && tile.block() == current.block && tile.build != null && tile.block().allowDerelictRepair && state.rules.derelictRepair) || !Structs.contains(current.block.requirements, (i)->!core.items.has(i.item, Math.min(Mathf.round(i.amount * state.rules.buildCostMultiplier), 1)));
                        if (hasAll) {
//                            Call.beginPlace(self(), current.block, team, current.x, current.y, current.rotation, current.block.instantBuild ? current.config : null);
                            if (!net.client() && current.block.instantBuild) {
                                if (plans.size > 0) {
                                    plans.removeFirst();
                                }
                                continue;
                            }
                        } else {
//                            current.stuck = true;
                        }
                    } else {
                        plans.removeFirst();
                        plans.addLast(current);
                        continue;
                    }
                } else if (!current.initialized && current.breaking && Build.validBreak(team, current.x, current.y)) {
//                    Call.beginBreak(self(), team, current.x, current.y);
                } else {
                    plans.removeFirst();
                    continue;
                }
            } else if ((tile.team() != team && tile.team() != Team.derelict) || (!current.breaking && (cb.current != current.block || cb.tile != current.tile()))) {
                plans.removeFirst();
                continue;
            }
            if (tile.build instanceof ConstructBlock.ConstructBuild && !current.initialized) {
//                Events.fire(new EventType.BuildSelectEvent(tile, team, self(), current.breaking));
//                current.initialized = true;
            }
            if (!(tile.build instanceof ConstructBlock.ConstructBuild entity)) {
                continue;
            }
//            float bs = 1.0F / entity.buildCost * type.buildSpeed * buildSpeedMultiplier * state.rules.buildSpeed(team);
            if (current.breaking) {
//                entity.deconstruct(self(), core, bs);
            } else if (entity.current != null && (state.isEditor() || (state.rules.waves && team == state.rules.waveTeam && entity.current.isVisible()) || (entity.current.unlockedNowHost() && entity.current.environmentBuildable() && entity.current.isPlaceable()))) {
//                entity.construct(self(), core, bs, current.config);
            }
//            current.stuck = Mathf.equal(current.progress, entity.progress);
//            current.progress = entity.progress;
        }
    }
}
