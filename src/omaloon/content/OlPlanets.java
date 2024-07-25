package omaloon.content;

import arc.math.*;
import arc.math.geom.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import omaloon.content.blocks.*;
import omaloon.graphics.g3d.*;
import omaloon.maps.*;
import omaloon.maps.ColorPass.*;
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
			atmosphereColor = OlEnvironmentBlocks.glacium.mapColor;

			Vec3 ringPos = new Vec3(0,1,0).rotate(Vec3.X, 25);

			generator = new GlasmorePlanetGenerator() {{
				baseHeight = 0;
				baseColor = OlEnvironmentBlocks.albaster.mapColor;

				Mathf.rand.setSeed(2);
				for(int i = 0; i < 10; i++) {
					int finalI = i;
					heights.add(new HeightPass.SphereHeight() {{
						pos.setToRandomDirection();
						radius = 0.3f + 0.025f * finalI;
						offset = 0.2f;
						set = true;
					}});
				}
				heights.add(
					new HeightPass.NoiseHeight() {{
						offset.set(1000, 0, 0);
						octaves = 7;
						persistence = 0.5;
						magnitude = 2;
						heightOffset = -1.25f;
					}}
				);
				for(int i = 0; i < 10; i++) {
					if (i + 11 == 19) continue;
					heights.add(new HeightPass.DotHeight() {{
						dir.set(0f, 1f, 0f).rotate(Vec3.X, 115f).rotate(ringPos, Mathf.random(360f));
						min = 0.93f;
						max = 0.99f;
						magnitude = 0.25f;
					}});
				}
				heights.add(
					new HeightPass.SphereHeight() {{
						pos.set(0f, 0.56f, -0.82f);
						radius = 0.3f;
						offset = 0f;
						set = true;
					}},
					new HeightPass.SphereHeight() {{
						pos.set(0.71f, 0.58f, -0.38f);
						radius = 0.2f;
						offset = 0f;
						set = true;
					}},
					new HeightPass.SphereHeight() {{
						pos.set(0f, 0.35f, 0.93f);
						radius = 0.2f;
						offset = 0f;
						set = true;
					}},
					new HeightPass.SphereHeight() {{
						pos.set(0.58f, 0.38f, 0.71f);
						radius = 0.2f;
						offset = 0f;
						set = true;
					}}
				);
				heights.add(new HeightPass.ClampHeight(0f, 1f));

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
					}}
				);
				for(int i = 0; i < 5; i++) {
					colors.add(new SphereColorPass(new Vec3().setToRandomDirection(), 0.06f, OlEnvironmentBlocks.grenite.mapColor));
				}
				colors.add(
					new FlatColorPass() {{
						min = max = 0f;
						out = OlEnvironmentBlocks.blueIce.mapColor;
					}}
				);
				heights.each(height -> height instanceof HeightPass.DotHeight, (HeightPass.DotHeight height) -> {
					colors.add(
						new ColorPass.SphereColorPass(height.dir, (height.max - height.min) * 2.5f, OlEnvironmentBlocks.grenite.mapColor),
						new ColorPass.SphereColorPass(height.dir, (height.max - height.min) * 1.7f, OlEnvironmentBlocks.glacium.mapColor)
					);
				});
				colors.add(new ColorPass.SphereColorPass(((HeightPass.DotHeight) heights.get(14)).dir, 0.06f * 2.5f, OlEnvironmentBlocks.blueIce.mapColor));
			}};

			meshLoader = () -> new MultiMesh(
					new HexMesh(this, 7),

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
