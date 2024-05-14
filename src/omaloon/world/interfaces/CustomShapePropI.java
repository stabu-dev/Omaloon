package omaloon.world.interfaces;

import mindustry.world.*;

public interface CustomShapePropI {
//	T getDefaultValue();

	default boolean nullableData() {
		return false;
	}

	void updateTile(Tile tile);
}
