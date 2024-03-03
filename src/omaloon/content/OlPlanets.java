package omaloon.content;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import omaloon.content.blocks.*;
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
					new ClampHeight(-0.2f, 1f)
				);
				Rand rand = new Rand(baseSeed);
				Seq<CraterHeight> craters = new Seq<>();
				for (int i = 0; i < 10; i++) {
					Vec2 pos = new Vec2().trns(rand.range(360f), 1);
					float y = rand.range(0.3f);
					CraterHeight crater = new CraterHeight(new Vec3(pos.x, y, pos.y), rand.random(0.3f), -rand.random(0.3f));
					heights.add(crater);
					craters.add(crater);
				}

				colors.add(
					new NoiseColorPass() {{
						scale = 1.5;
						persistence = 0.5;
						octaves = 3;
						magnitude = 1.2f;
						min = 0.3f;
						max = 0.6f;
						out = OlEnvironmentBlocks.quartzSand.mapColor;
						offset.set(1500f, 300f, -500f);
					}},
					new NoiseColorPass() {{
						seed = 5;
						scale = 1.5;
						persistence = 0.5;
						octaves = 3;
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
					}}
				);
				craters.each(crater -> colors.add(new CraterColorPass(crater.position, crater.radius - 0.05f, OlEnvironmentBlocks.dalani.mapColor)));
			}};
			meshLoader = () -> new HexMesh(this, 6);
			cloudMeshLoader = () -> new MultiMesh(
				new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.8f), 2, 0.42f, 1f, 0.6f),
				new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.6f), 2, 0.42f, 1.2f, 0.5f),
			new HexSkyMesh(this, 2, -0.2f, 0.14f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.8f), 2, 0.42f, 1f, 0.4f),
				new HexSkyMesh(this, 4, 0.4f, 0.15f, 6, OlEnvironmentBlocks.blueIce.mapColor.cpy().a(0.6f), 2, 0.42f, 1.2f, 0.2f)
			);
		}};
	}
}
