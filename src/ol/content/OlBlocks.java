package ol.content;

import ol.content.blocks.*;

public class OlBlocks implements Runnable {
	public Runnable[] list = {
			OlEnvironment::load,
			OlDistribution::load,
			OlProduction::load,
			OlPower::load,
			OlDefence::load,
	};

	public static void load() {
	}

	@Override
	public void run() {
		load();
	}
}
