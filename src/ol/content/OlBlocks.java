package ol.content;

import ol.content.blocks.*;

public class OlBlocks {

	public static void load() {
		OlEnvironment.load();
		OlPressure.load();
		OlProduction.load();
		OlPower.load();
		OlDefence.load();
	}
}
