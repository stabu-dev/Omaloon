package omaloon.content;

import arc.*;
import arc.audio.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.game.EventType.*;
import omaloon.content.blocks.*;

public class OlMusics {
	private static Music[] vanillaAmbient;
	private static Music[] vanillaDark;
	private static Music[] vanillaBoss;

	public static Music
	//vanilla
	launch, land,
	//omaloon launch
	orbital,

	//glasmore music
	glLand,
	//ambient
	chained, darkPurity, misdom, space, sundown,
	//dark
	fragile, solidFire, soredLuna,
	//boss
	buryAlive, chaoticFlames, liquefy, piercingLine,

	end;

	public static void load() {
		//vanilla
		launch = Musics.launch;
		land = Musics.land;

		//omaloon
		orbital = Vars.tree.loadMusic("orbital");

		//glasmore
		glLand = loadMusic("landings/glasmore-land");

		//ambient
		chained = loadMusic("glasmore/ambient/chained");
		darkPurity = loadMusic("glasmore/ambient/darkPurity");
		misdom = loadMusic("glasmore/ambient/misdom");
		space = loadMusic("glasmore/ambient/space");
		sundown = loadMusic("glasmore/ambient/sundown");
		//dark
		fragile = loadMusic("glasmore/dark/fragile");
		solidFire = loadMusic("glasmore/dark/solidFire");
		soredLuna = loadMusic("glasmore/dark/soredLuna");
		//boss
		buryAlive = loadMusic("glasmore/boss/buryAlive");
		chaoticFlames = loadMusic("glasmore/boss/chaoticFlames");
		liquefy = loadMusic("glasmore/boss/liquefy");
		piercingLine = loadMusic("glasmore/boss/piercingLine");

		Events.run(EventType.Trigger.update, () -> {
			if ((Vars.ui.planet.state.planet == OlPlanets.omaloon) || Vars.ui.planet.state.planet == OlPlanets.glasmore) {
				Musics.launch = orbital;
			} else {
				Musics.launch = launch;
			}
		});

		Events.on(WorldLoadEvent.class, e -> {
			Vars.state.rules.defaultTeam.cores().each(core -> {
				if(core.block == OlStorageBlocks.landingCapsule || core.block == OlStorageBlocks.coreFloe) {
					Musics.land = glLand;
				} else {
					Musics.land = land;
				}
			});
		});

		vanillaAmbient = Vars.control.sound.ambientMusic.toArray(Music.class);
		vanillaDark = Vars.control.sound.darkMusic.toArray(Music.class);
		vanillaBoss = Vars.control.sound.bossMusic.toArray(Music.class);

		Events.on(WorldLoadEvent.class, e -> {
			if (Vars.state.rules.planet == OlPlanets.glasmore) {
				Vars.control.sound.ambientMusic.clear();
				Vars.control.sound.ambientMusic.addAll(chained, darkPurity, misdom, space, sundown);

				Vars.control.sound.darkMusic.clear();
				Vars.control.sound.darkMusic.addAll(fragile, solidFire, soredLuna);

				Vars.control.sound.bossMusic.clear();
				Vars.control.sound.bossMusic.addAll(buryAlive, chaoticFlames, liquefy, piercingLine);
			} else {
				Vars.control.sound.ambientMusic.clear();
				Vars.control.sound.ambientMusic.addAll(vanillaAmbient);

				Vars.control.sound.darkMusic.clear();
				Vars.control.sound.darkMusic.addAll(vanillaDark);

				Vars.control.sound.bossMusic.clear();
				Vars.control.sound.bossMusic.addAll(vanillaBoss);
			}
		});
	}

	private static Music loadMusic(String name) {
		return Vars.tree.loadMusic(name);
	}
}