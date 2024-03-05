package omaloon.content;

import arc.math.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import omaloon.content.blocks.*;
import omaloon.maps.ColorPass.*;
import omaloon.maps.HeightPass.*;
import omaloon.maps.planets.*;

public class OlPlanets {
	public static Planet glassmore;

	public static void load() {
		glassmore = new Planet("glassmore", Planets.sun, 1f, 2) {{
			//TODO remove in release
			PlanetDialog.debugSelect = true;

			atmosphereRadIn = -0.05f;
			atmosphereRadOut = 0.3f;
			atmosphereColor = OlEnvironmentBlocks.dalani.mapColor;
			generator = new GlassmorePlanetGenerator() {{
				baseHeight = -1f;
				baseColor = OlEnvironmentBlocks.albaster.mapColor;
				heights.addAll(
					new AngleInterpHeight() {{
						interp = new Interp.ExpIn(2, 10);
						dir.set(1f, 0f, 0f);
						magnitude = 5;
					}},
					new AngleInterpHeight() {{
						interp = new Interp.ExpIn(2, 10);
						dir.set(-0.5f, 0.5f, 1);
						magnitude = 5;
					}},
					new AngleInterpHeight() {{
						interp = new Interp.ExpIn(2, 10);
						dir.set(-0.3f, -1f, -0.6f);
						magnitude = 5;
					}},
					new ClampHeight(0f, 0.8f),
					new NoiseHeight() {{
						scale = 1.5;
						persistence = 0.5;
						octaves = 3;
						magnitude = 1.2f;
						heightOffset = -1f;
						offset.set(1500f, 300f, -500f);
					}},
					new ClampHeight(-0.2f, 0.8f),
					new CraterHeight(new Vec3(-0.5f, 0.25f, 1f), 0.3f, -0.3f),
					new CraterHeight(new Vec3(-0.3f, 0.5f, 0.8f), 0.17f, 0.2f) {{
						set = true;
					}},
					new CraterHeight(new Vec3(1f, 0f, 0.6f), 0.17f, 0.1f) {{
						set = true;
					}},
					new CraterHeight(new Vec3(1f, 0f, 0f), 0.17f, -0.2f)
				);

				colors.addAll(
					new NoiseColorPass() {{
						scale = 1.5;
						persistence = 0.5;
						octaves = 3;
						magnitude = 1.2f;
						min = 0.3f;
						max = 0.6f;
						out = OlEnvironmentBlocks.deadGrass.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 5;
						scale = 1.5;
						persistence = 0.5;
						octaves = 5;
						magnitude = 1.2f;
						min = 0.1f;
						max = 0.4f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 8;
						scale = 1.5;
						persistence = 0.5;
						octaves = 7;
						magnitude = 1.2f;
						min = 0.1f;
						max = 0.4f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new FlatColorPass() {{
						min = -1f;
						max = -0.19f;
						out = OlEnvironmentBlocks.blueIce.mapColor;
					}},
					new CraterColorPass(new Vec3(-0.5f, 0.25f, 1f), 0.4f, OlEnvironmentBlocks.grenite.mapColor),
					new CraterColorPass(new Vec3(-0.3f, 0.5f, 0.8f), 0.1f, OlEnvironmentBlocks.dalani.mapColor),
					new CraterColorPass(new Vec3(1f, 0f, 0.6f), 0.2f, OlEnvironmentBlocks.grenite.mapColor),
					new CraterColorPass(new Vec3(1f, 0f, 0f), 0.25f, OlEnvironmentBlocks.grenite.mapColor)
				);
			}};
			meshLoader = () -> new HexMesh(this, 7);
			cloudMeshLoader = () -> new MultiMesh(
				new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1f, 0.6f),
				new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1.2f, 0.5f)
			);
		}};
	}
}
