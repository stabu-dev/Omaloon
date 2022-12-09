package ol.content;

import ol.content.blocks.*;

public class OlBlocks implements Runnable{
	public Runnable[] list = {
			OlEnvironment::load,
			OlDefence::load,
			OlPower::load,
			OlProduction::load,
			OlPressure::load,
	};
	public static void load(){
	}
	@Override
	public void run(){
		load();
	}
}
