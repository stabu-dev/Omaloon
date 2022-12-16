package ol.content;

import ol.content.blocks.*;

public class OlBlocks /*implements Runnable*/ {
	//public static Runnable[] list = {
	//		OlEnvironment::load,
	//		OlDistribution::load,
	//		OlProduction::load,
	//		OlPower::load,
	//		OlDefence::load,
	//};

	public static void load() {
		//why need make code harder and slower when you can just invoke it in load
		//and why only OlBlocks implements Runnable?
		//why if you can just call load method

		OlEnvironment  .load();
		OlDistribution .load();
		OlProduction   .load();
		OlPower        .load();
		OlDefence      .load();
	}

	//@Override
	//public void run() {
	//	load();
	//}
}
