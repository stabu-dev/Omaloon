package Ol.world.blocks.power;

import arc.math.*;
import arc.struct.*;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class OlPanel extends PowerGenerator {

    public OlPanel(String name){
        super(name);
        //remove the BlockFlag.generator flag to make this a lower priority target than other generators.
        flags = EnumSet.of(BlockFlag.generator);
        envEnabled = Env.any;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }

    public class olPanelBuild extends GeneratorBuild {
        @Override
        public void updateTile() {
            productionEfficiency = enabled ?
                    Mathf.maxZero(Attribute.light.env() +
                            (state.rules.lighting ?
                                    1f - state.rules.ambientLight.a :
                                    1f
                            )) : 0f;
            if(liquids.currentAmount() > 0.01f){
                dumpLiquid(liquids.current());
            }

        }

        @Override
        public float getPowerProduction() {
            return super.getPowerProduction() * efficiency;
        }


        public void dumpLiquid(Liquid liquid, float scaling, int outputDir){
            int dump = this.cdump;

            if(liquids.get(liquid) <= 0.0001f) return;

            if(!net.client() && state.isCampaign() && team == state.rules.defaultTeam) liquid.unlock();

            for(int i = 0; i < proximity.size; i++){
                incrementDump(proximity.size);

                Building other = proximity.get((i + dump) % proximity.size);
                if(outputDir != -1 && (outputDir + rotation) % 4 != relativeTo(other)) continue;
                if (!(other.block instanceof OlPanel))continue;

                other = other.getLiquidDestination(self(), liquid);

                if(other != null && other.team == team && other.block.hasLiquids && canDumpLiquid(other, liquid) && other.liquids != null){
                    float ofract = other.liquids.get(liquid) / other.block.liquidCapacity;
                    float fract = liquids.get(liquid) / block.liquidCapacity;

                    if(ofract < fract) transferLiquid(other, (fract - ofract) * block.liquidCapacity / scaling, liquid);
                }
            }
        }
    }
}
