package omaloon.content;

import arc.math.Interp;
import arc.math.geom.*;
import arc.scene.ui.Dialog;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import omaloon.content.blocks.*;
import omaloon.graphics.g3d.*;
import omaloon.maps.ColorPass.*;
import omaloon.maps.HeightPass.*;
import omaloon.maps.planets.*;

import static arc.Core.*;
import static arc.graphics.Color.*;

public class OlPlanets {
	public static Planet omaloon, glasmore;

	public static void load() {
		//TODO remove in release
		PlanetDialog.debugSelect = true;

		omaloon = new Planet("omaloon", null, 4f, 0) {{
			bloom = true;
			accessible = false;
			hasAtmosphere = true;
			solarSystem = this;

			meshLoader = () -> new SunMesh(
					this, 4, 5, 0.3f, 1.0f, 1.2f, 1, 1.3f,

					valueOf("#8B4513"),
					valueOf("#A0522D"),
					valueOf("c2311e"),
					valueOf("ff6730"),
					valueOf("bf342f"),
					valueOf("8e261d")
			);
		}};

		glasmore = new Planet("glasmore", omaloon, 1f, 4){{
			icon = "glasmore";
			solarSystem = omaloon;
			orbitRadius = 40f;
			atmosphereRadIn = -0.05f;
			atmosphereRadOut = 0.3f;
			atmosphereColor = OlEnvironmentBlocks.dalani.mapColor;
			generator = new GlasmorePlanetGenerator() {{
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
						minNoise = 0.3f;
						maxNoise = 0.6f;
						out = OlEnvironmentBlocks.deadGrass.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 5;
						scale = 1.5;
						persistence = 0.5;
						octaves = 5;
						magnitude = 1.2f;
						minNoise = 0.1f;
						maxNoise = 0.4f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 8;
						scale = 1.5;
						persistence = 0.5;
						octaves = 7;
						magnitude = 1.2f;
						minNoise = 0.1f;
						maxNoise = 0.4f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new FlatColorPass() {{
						minHeight = -1f;
						maxHeight = -0.19f;
						out = OlEnvironmentBlocks.blueIce.mapColor;
					}},
					new CraterColorPass(new Vec3(-0.5f, 0.25f, 1f), 0.4f, OlEnvironmentBlocks.grenite.mapColor),
					new CraterColorPass(new Vec3(-0.3f, 0.5f, 0.8f), 0.1f, OlEnvironmentBlocks.dalani.mapColor),
					new CraterColorPass(new Vec3(1f, 0f, 0.6f), 0.2f, OlEnvironmentBlocks.grenite.mapColor),
					new CraterColorPass(new Vec3(1f, 0f, 0f), 0.25f, OlEnvironmentBlocks.grenite.mapColor)
				);
			}};

			Vec3 ringPos = new Vec3(0,1,0).rotate(Vec3.X, 25);

			meshLoader = () -> new MultiMesh(
					new HexMesh(this, 6),

					new CircleMesh(atlas.find("omaloon-ring4"), this, 80, 2.55f, 2.6f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring3"), this,80, 2.2f, 2.5f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring2"), this,80, 1.9f, 2.1f, ringPos),
					new CircleMesh(atlas.find("omaloon-ring1"), this,80, 1.8f, 1.85f, ringPos)
			);

			cloudMeshLoader = () -> new MultiMesh(
					new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1f, 0.6f),
					new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.2f), 2, 0.42f, 1.2f, 0.5f)
			);
		}};
	}
}
