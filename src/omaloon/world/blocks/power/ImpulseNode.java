package omaloon.world.blocks.power;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

public class ImpulseNode extends PowerNode{

    public int effectTimer = timers++;
    public float effectTime = 20;

    public ImpulseNode(String name){
        super(name);
    }

    public class ImpulseNodeBuild extends PowerNodeBuild{

        @Override
        public void draw(){
            Draw.rect(region, x, y, this.drawrot());

            if(Mathf.zero(Renderer.laserOpacity) || isPayload()) return;

            Draw.z(Layer.power);
            setupColor(power.graph.getSatisfaction());

            for(int i = 0; i < power.links.size; i++){
                Building link = world.build(power.links.get(i));

                if(!linkValid(this, link)) continue;

                if(link.block instanceof PowerNode && link.id >= id) continue;

                Draw.alpha(Renderer.laserOpacity / 2f);
                drawLaser(x, y, link.x, link.y, size, link.block.size);

                if(timer.get(effectTimer, effectTime) && power.graph.getSatisfaction() > 0){
                    for(int l = 0; l < power.links.size; l++){
                        Tile otherTile = Vars.world.tile(power.links.get(l));
                        if(otherTile != null){
                            Building other = otherTile.build;
                            if(other != null){
                                float angle1 = Angles.angle(x, y, other.x, other.y),
                                vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
                                len1 = size * tilesize / 2f - 1.5f, len2 = other.block.size * tilesize / 2f - 1.5f;

                                lightning(x + vx * len1, y + vy * len1,
                                other.x - vx * len2, other.y - vy * len2,
                                2, Mathf.random(-8f, 8f),
                                laserColor2.cpy().lerp(laserColor1, power.graph.getSatisfaction()).a(Renderer.laserOpacity),
                                Fx.lightning.layer(Layer.power));
                            }
                        }
                    }
                }
            }

            Draw.reset();
        }

        /**
         * Original code from Project HPL[<a href="https://github.com/HPL-Team/Project-HPL">...</a>]
         */
        public void lightning(float x1, float y1, float x2, float y2, int iterations, float rndScale, Color c, Effect e){
            Seq<Vec2> lines = new Seq<>();
            boolean swap = Math.abs(y1 - y2) < Math.abs(x1 - x2);
            if(swap){
                lines.add(new Vec2(y1, x1));
                lines.add(new Vec2(y2, x2));
            }else{
                lines.add(new Vec2(x1, y1));
                lines.add(new Vec2(x2, y2));
            }
            for(int i = 0; i < iterations; i++){
                for(int j = 0; j < lines.size - 1; j += 2){
                    Vec2 v1 = lines.get(j), v2 = lines.get(j + 1);
                    Vec2 v = new Vec2((v1.x + v2.x) / 2, ((v1.y + v2.y) / 2));
                    float ang = (v2.angle(v1) + 90f) * Mathf.degRad;
                    float sin = Mathf.sin(ang), cos = Mathf.cos(ang);
                    float rnd = Mathf.random(rndScale);
                    v.x += rnd * sin;
                    v.y += rnd * cos;
                    lines.insert(j + 1, v);
                }
            }
            if(swap){
                for(int i = 0; i < lines.size; i++){
                    Vec2 v = lines.get(i);
                    float px = v.x;
                    v.x = v.y;
                    v.y = px;
                }
            }
            e.at(x1, y1, 0f, c, lines);
        }
    }
}
