package omaloon.type;

import arc.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.type.*;

public class ExtraSectorPreset extends SectorPreset {
	public static ObjectMap<String, Runnable> scripts = new ObjectMap<>();

	static {
		Events.run(EventType.Trigger.update, () -> {
			scripts.get(Vars.state.map.name(), () -> {}).run();
		});
	}

	public ExtraSectorPreset(String name, Planet planet, int sector, Runnable run) {
		this(name, planet, sector);
		scripts.put(generator.map.name(), run);
	}

	public ExtraSectorPreset(String name, Planet planet, int sector) {
		super(name, planet, sector);
	}
}
