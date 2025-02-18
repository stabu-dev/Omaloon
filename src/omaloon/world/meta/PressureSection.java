package omaloon.world.meta;

import arc.math.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.math.*;
import omaloon.world.interfaces.*;

public class PressureSection{
    public Seq<HasPressure> builds = new Seq<>();

    /**
     * Temporary seqs for use in flood.
     */
    public static final Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>();

    /**
     * Adds a build to this section, and removes the build from its older section.
     */
    public void addBuild(HasPressure build){
        builds.add(build);
        build.pressure().section.builds.remove(build);
        build.pressure().section = this;
    }

    /**
     * Adds a certain amount of a fluid into this module, updating it's pressure accordingly. A null liquid means that air is being added to it.
     */
    public void addFluid(@Nullable Liquid liquid, float amount){
        if(builds.isEmpty()) return;
        float div = amount / builds.size;
        builds.each(b -> b.pressure().addFluid(liquid, div, b.pressureConfig()));
    }

    /**
     * Merges buildings to this section with floodFill.
     */
    public void mergeFlood(HasPressure other){
        tmp.clear().add(other);
        tmp2.clear();

        while(!tmp.isEmpty()){
            HasPressure next = tmp.pop();
            tmp2.addUnique(next);
            next.nextBuilds().each(b -> {
                if(b.getSectionDestination(next) != null && !tmp2.contains(b.getSectionDestination(next))){
                    tmp.add(b.getSectionDestination(next));
                }
            });
        }

        tmp2.each(this::addBuild);
        updateLiquids();
    }

    /**
     * Removes a certain amount of a fluid from this module, updating pressure accordingly. A null liquid means that air is being removed from it. Liquids cannot be negative.
     */
    public void removeFluid(@Nullable Liquid liquid, float amount){
        if(builds.isEmpty()) return;
        float div = amount / builds.size;
        builds.each(b -> b.pressure().removeFluid(liquid, div, b.pressureConfig()));
    }

    public void updateLiquids(){
        float[] liquids = new float[Vars.content.liquids().size];
        float air = 0;

        for(Liquid liquid : Vars.content.liquids())
            for(HasPressure build : builds){
                liquids[liquid.id] += build.pressure().liquids[liquid.id];
                build.pressure().liquids[liquid.id] = 0;
                build.pressure().pressures[liquid.id] = 0;
            }
        for(HasPressure build : builds){
            air += build.pressure().air;
            build.pressure().air = 0;
            build.pressure().pressure = 0;
        }

        for(Liquid liquid : Vars.content.liquids()) addFluid(liquid, liquids[liquid.id]);
        addFluid(null, air);
    }

    public void updateTransfer(){
        Seq<Entry<HasPressure, HasPressure>> links = new Seq<>();

        builds.each(b -> {
            b.nextBuilds().retainAll(other -> other.pressure().section != this).each(other -> {
                links.add(new Entry<>(){{
                    key = other;
                    value = b;
                }});
            });
        });

        FloatSeq amounts = new FloatSeq();

        Vars.content.liquids().each(main -> {
            amounts.clear();
            for(Entry<HasPressure, HasPressure> entry : links){
                float flow = OlMath.flowRate(
                entry.value.pressure().getPressure(main),
                entry.key.pressure().getPressure(main),
                entry.value.pressureConfig().fluidCapacity,
                entry.key.pressureConfig().fluidCapacity,
                OlLiquids.getDensity(main),
                OlLiquids.getViscosity(main)
                ) / (2f * links.size);

                if(
                (
                flow > 0 ?
                (
                entry.key.acceptsPressurizedFluid(entry.value, main, flow) &&
                entry.value.outputsPressurizedFluid(entry.key, main, flow)
                ) :
                (
                entry.value.acceptsPressurizedFluid(entry.key, main, flow) &&
                entry.key.outputsPressurizedFluid(entry.value, main, flow)
                )
                ) &&
                (entry.key.pressure().get(main) > 0 || entry.value.pressure().get(main) > 0)
                ){
                    amounts.add(flow);
                }else{
                    amounts.add(0);
                }
            }
            for(Entry<HasPressure, HasPressure> entry : links){
                float maxFlow = OlMath.flowRate(
                entry.value.pressure().getPressure(main),
                entry.key.pressure().getPressure(main),
                entry.value.pressureConfig().fluidCapacity,
                entry.key.pressureConfig().fluidCapacity,
                OlLiquids.getDensity(main),
                1
                ) / (2f * links.size);

                float flow = Mathf.clamp(
                amounts.get(links.indexOf(entry)) * Time.delta,
                -Math.abs(maxFlow),
                Math.abs(maxFlow)
                );
                if(flow != 0){
                    entry.key.addFluid(main, flow);
                    entry.value.removeFluid(main, flow);
                }
            }
        });
        amounts.clear();
        for(Entry<HasPressure, HasPressure> entry : links){
            float flow = OlMath.flowRate(
            entry.value.pressure().getPressure(null),
            entry.key.pressure().getPressure(null),
            entry.value.pressureConfig().fluidCapacity,
            entry.key.pressureConfig().fluidCapacity,
            OlLiquids.getDensity(null),
            OlLiquids.getViscosity(null)
            ) / (2f * links.size);

            if(
            (
            flow > 0 ?
            (
            entry.key.acceptsPressurizedFluid(entry.value, null, flow) &&
            entry.value.outputsPressurizedFluid(entry.key, null, flow)
            ) : (
            entry.value.acceptsPressurizedFluid(entry.key, null, flow) &&
            entry.key.outputsPressurizedFluid(entry.value, null, flow)
            )
            )
            ){
                amounts.add(flow);
            }else{
                amounts.add(0);
            }
        }
        for(Entry<HasPressure, HasPressure> entry : links){
            float maxFlow = OlMath.flowRate(
            entry.value.pressure().getPressure(null),
            entry.key.pressure().getPressure(null),
            entry.value.pressureConfig().fluidCapacity,
            entry.key.pressureConfig().fluidCapacity,
            OlLiquids.getDensity(null),
            1
            ) / (2f * links.size);

            float flow = Mathf.clamp(
            amounts.get(links.indexOf(entry)) * Time.delta,
            -Math.abs(maxFlow),
            Math.abs(maxFlow)
            );
            if(flow != 0){
                entry.key.addFluid(null, amounts.get(links.indexOf(entry)));
                entry.value.removeFluid(null, amounts.get(links.indexOf(entry)));
            }
        }
    }
}
