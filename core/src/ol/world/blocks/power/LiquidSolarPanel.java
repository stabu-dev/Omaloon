package ol.world.blocks.power;

import arc.math.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class LiquidSolarPanel extends PowerGenerator {
    public LiquidSolarPanel(String name){
        super(name);
        ambientSound = Sounds.none;
        hasLiquids = true;
    }

    public class LiquidSolarPanelBuild extends GeneratorBuild {

        @Override
        public void updateTile() {
            float currentAmount = liquids.currentAmount();
            if (currentAmount > 0f) {
                float liquidCapacity = block.liquidCapacity;
                productionEfficiency = enabled ?
                        Mathf.clamp(currentAmount / liquidCapacity, 0f, 1f)
                                * (Attribute.light.env() + (state.rules.lighting ? 1f - state.rules.ambientLight.a : 1f)
                        ) : 0f;
            } else {
                productionEfficiency = 0f;
            }

            if (currentAmount > 0.01f) {
                dumpLiquid(liquids.current());
            }
        }

        @Override
        public float getPowerProduction() {
            return super.getPowerProduction() * efficiency;
        }

        public void dumpLiquid(Liquid liquid) {
            float dump = this.cdump;

            if (liquids.get(liquid) <= 0.0001f) {
                return;
            }

            if (!net.client() && state.isCampaign() && team == state.rules.defaultTeam) {
                liquid.unlock();
            }

            for (int i = 0; i < proximity.size; i++) {
                incrementDump(proximity.size);

                Building other = proximity.get((i + (int) dump) % proximity.size);

                if (!(other.block instanceof LiquidSolarPanel)) {
                    continue;
                }

                other = other.getLiquidDestination(self(), liquid);

                if (other != null && other.team == team && other.block.hasLiquids && canDumpLiquid(other, liquid) && other.liquids != null) {
                    float ofract = other.liquids.get(liquid) / other.block.liquidCapacity;
                    float fract = liquids.get(liquid) / block.liquidCapacity;

                    if (ofract < fract) {
                        transferLiquid(other, fract - ofract, liquid);
                    }
                }
            }
        }
    }
}