package omaloon.content;

import arc.*;
import arc.audio.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;

public class OlMusics {
	public static Music orbital;

	public static Music oldLaunch;

	public static void load() {
		orbital = Vars.tree.loadMusic("orbital");

		oldLaunch = Musics.launch;

		Events.run(EventType.Trigger.update, () -> {
			if (Vars.ui.planet.state.planet == OlPlanets.glasmore) {
				Musics.launch = orbital;
			} else {
				Musics.launch = oldLaunch;
			}
		});
	}
}
