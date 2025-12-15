package omaloon.ui;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.ui.fragments.HintsFragment.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import omaloon.content.blocks.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.blocks.power.*;
import omaloon.world.interfaces.*;

public enum EventHints implements Hint{
    air(
    () -> false,
    () -> Vars.state.rules.defaultTeam.data().buildings.contains(b -> b instanceof HasPressure p && p.pressureConfig().fluidCapacity > 8f)
    ),
    drills(
    () -> false,
    () -> Vars.control.input.block == OlProductionBlocks.hammerDrill
    ),
    pump_chaining(
    () -> false,
    () -> Vars.control.input.block instanceof PressureLiquidPump &&
    Vars.state.rules.defaultTeam.data().buildings.contains(b -> b.block instanceof PressureLiquidPump)
    ),
    wind_turbines(
    () -> false,
    () -> {
        if(Vars.control.input.block != OlPowerBlocks.windTurbine) return false;

        AreaGenerator block = (AreaGenerator)OlPowerBlocks.windTurbine;
        int x = World.toTile(Vars.player.mouseX), y = World.toTile(Vars.player.mouseY);
        int r = block.range + Mathf.ceil(block.size / 2f) + ((block.size + 1) % 2);

        if(block.checkNearby(x, y, t -> t != null && (t.block() == block || (t.build instanceof ConstructBuild cb && cb.current == block)))) return true;

        for(BlockPlan p : Vars.player.team().data().plans)
            if(p.block == block && Math.abs(p.x - x) < r && Math.abs(p.y - y) < r) return true;

        for(Unit u : Groups.unit)
            if(u.team == Vars.player.team())
                for(BuildPlan p : u.plans())
                    if(p.block == block && Math.abs(p.x - x) < r && Math.abs(p.y - y) < r) return true;

        return false;
    }
    ),
    press(
    () -> false,
    () -> Vars.control.input.block == OlCraftingBlocks.compositePress
    ),
    shelter(
    () -> false,
    () -> Vars.control.input.block instanceof Shelter
    );

    static final String prefix = "omaloon-";
    final Boolp complete;
    Boolp shown = () -> true;
    EventHints[] requirements;
    int visibility = visibleAll;
    boolean cached, finished;

    EventHints(Boolp complete){
        this.complete = complete;
    }

    EventHints(Boolp complete, Boolp shown){
        this(complete);
        this.shown = shown;
    }

    EventHints(Boolp complete, Boolp shown, EventHints... requirements){
        this(complete, shown);
        this.requirements = requirements;
    }

    public static void addHints(){
        Vars.ui.hints.hints.add(Seq.with(EventHints.values()).removeAll(
        hint -> Core.settings.getBool(prefix + hint.name() + "-hint-done", false)
        ));
    }

    public static void reset(){
        for(EventHints hint : values()){
            Core.settings.put(prefix + hint.name() + "-hint-done", hint.finished = false);
        }
        addHints();
    }

    @Override
    public boolean complete(){
        return complete.get();
    }

    @Override
    public void finish(){
        Core.settings.put(prefix + name() + "-hint-done", finished = true);
    }

    @Override
    public boolean finished(){
        if(!cached){
            cached = true;
            finished = Core.settings.getBool(prefix + name() + "-hint-done", false);
        }
        return finished;
    }

    @Override
    public int order(){
        return ordinal();
    }

    @Override
    public boolean show(){
        return shown.get() && (requirements == null || (requirements.length == 0 || !Structs.contains(requirements, d -> !d.finished())));
    }

    @Override
    public String text(){
        return Core.bundle.get("hint." + prefix + name(), "Missing bundle for hint: hint." + prefix + name());
    }

    @Override
    public boolean valid(){
        return (Vars.mobile && (visibility & visibleMobile) != 0) || (!Vars.mobile && (visibility & visibleDesktop) != 0);
    }
}
