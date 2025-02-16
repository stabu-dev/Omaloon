package omaloon.net;

import arc.util.*;
import arc.util.CommandHandler.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;
import omaloon.gen.*;

public class OlServer{
    public static void registerServerCommands(CommandHandler handler){


    }

    public static void registerClientCommands(CommandHandler handler){
        handler.register("debug_id", "", command((args, player) -> {
            player.sendMessage(player.unit().id + "");
            for(Ability ability : player.unit().abilities){
                if(ability instanceof DroneAbility droneAbility){
                    for(Unit drone : droneAbility.drones){
                        player.sendMessage(" - "+drone.id);
                    }
                }
            }
            player.sendMessage("------------");
            for(Unit unit : Groups.unit){
                if(unit instanceof Dronec drone){
                    player.sendMessage("|"+drone.id());
                }
            }
            player.sendMessage("------------");
        }));
    }

    private static CommandRunner<Player> command(CommandRunner<Player> playerCommandRunner){
        return playerCommandRunner;
    }
}
