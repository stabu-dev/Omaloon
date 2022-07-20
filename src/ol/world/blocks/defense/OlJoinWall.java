package ol.world.blocks.defense;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.BoolSeq;
import arc.struct.IntMap;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stat;

public class OlJoinWall extends OlWall {
    protected static final int defaultKey = "00000000".hashCode();

    protected static final int[] needCheckPoint = {4, 5, 6, 7};

    protected static final int[][] tileKey = {
            {5, 1, 4},
            {2,    0},
            {6, 3, 7}
    };

    //DO NOT USE Geometry.d8. This array is designed to make the load and match method more brief.
    protected static final Point2[] traverseKey = {
            new Point2(1, 0),
            new Point2(0, 1),
            new Point2(-1, 0),
            new Point2(0, -1),

            //Edge Points Needed To Be Checked. Index from 4 to 7.

            new Point2(1, 1),
            new Point2(-1, 1),
            new Point2(-1, -1),
            new Point2(1, -1)
    };

    public int linkMaxIteration = 1;
    public float linkAlphaLerpDst = 24f;
    public float linkAlphaScl = 0.45f;
    public float minShareDamage = 70;

    public final IntMap<TextureRegion> sprites = new IntMap<>();

    public OlJoinWall(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();
        sprites.put(defaultKey, Core.atlas.find(name));

        loop: for(int i = 0; i < 256; i ++){
            String key = Integer.toBinaryString(i);
            StringBuilder builder = new StringBuilder();
            for(int j = 0; j < 8 - key.length(); j++)builder.append(0);
            builder.append(key);
            for(int j : needCheckPoint){
                if(builder.charAt(j) == '0')continue;
                if(builder.charAt((j - 3) % 4) != '1' || builder.charAt(j - 4) != '1')continue loop;
            }
            key = builder.toString();
            if(key.startsWith("0000"))continue;
            sprites.put(key.hashCode(), Core.atlas.find(name + "-" + key));
        }
    }

    public class ShapeWallBuild extends Building{
        public Seq<ShapeWallBuild> connectedWalls = new Seq<>();
        public transient BoolSeq proximityWalls = new BoolSeq(8);
        public transient TextureRegion currentRegion = region;
        protected int drawKey = defaultKey;

        public void updateKey(){
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < 8; i++){
                builder.append(Mathf.num(proximityWalls.get(i)));
            }

            for(int i : needCheckPoint){
                if(builder.charAt((i - 3) % 4) != '1' || builder.charAt((i - 4) % 4) != '1')builder.setCharAt(i, '0');
            }

            String key = builder.toString();
            drawKey = key.hashCode();

            if(key.startsWith("0000"))drawKey = defaultKey;

            currentRegion = sprites.get(drawKey);
        }

        public void computePoint(Point2 point){
			/*
				5 1 4
				2 x 0
				6 3 7
			 */
            int x = point.x, y = point.y;

            for(int i = 0; i < traverseKey.length; i++){
                if(point.equals(traverseKey[i])){
                    proximityWalls.set(i, !proximityWalls.get(i));
                    break;
                }
            }

            updateKey();
        }

        public void updateIndexKey(boolean add){
            for(Point2 index : traverseKey){
                Building build = Vars.world.build(tileX() + index.x, tileY() + index.y);
                if(build instanceof ShapeWallBuild){
                    computePoint(Tmp.p1.set(build.tileX() - tileX(), build.tileY() - tileY()));
                    ((ShapeWallBuild)build).computePoint(Tmp.p1.set(tileX() - build.tileX(), tileY() - build.tileY()));
                }
            }

            updateConnection(add);
        }

        public OrderedSet<ShapeWallBuild> getConnections(int iteration){
            OrderedSet<ShapeWallBuild> builds = new OrderedSet<>();
            builds.addAll(connectedWalls);

            if(iteration > 0){
                for(ShapeWallBuild build : connectedWalls){
                    builds.addAll(build.getConnections(iteration - 1));
                }
            }

            return builds;
        }

        public void updateConnection(boolean add){
            for(Point2 index : traverseKey){
                Building build = Vars.world.build(tileX() + index.x, tileY() + index.y);
                if(build instanceof ShapeWallBuild b){

                    if(add)b.connectedWalls.add(this);
                    else b.connectedWalls.remove(this);

                    connectedWalls.add(b);
                }
            }
        }

        public void drawTeam() {
            Draw.color(this.team.color);
            Fill.poly(x, y, 6, 1);
            Draw.color();
        }

        @Override
        public boolean collision(Bullet other){
            if(other.type.absorbable)other.absorb();
            return super.collision(other);
        }

        @Override
        public void draw(){
            Draw.rect(currentRegion, x, y);
        }

        @Override
        public void created(){
            super.created();
            updateConnection(true);
            if(!Vars.headless){
                initSeq();
                updateIndexKey(true);
            }
        }

        public void initSeq(){
            if(proximityWalls.size != 8){
                proximityWalls = new BoolSeq(8);
                for(int i = 0; i < 8; i++){
                    proximityWalls.add(false);
                }
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            if(!Vars.headless){
                initSeq();
                updateIndexKey(false);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(drawKey);
            if(proximityWalls.size == 8)for(int i = 0; i < 8; i++){
                write.bool(proximityWalls.get(i));
            }else for(int i = 0; i < 8; i++){
                write.bool(false);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            drawKey = read.i();
            for(int i = 0; i < 8; i++){
                proximityWalls.add(read.bool());
            }

            currentRegion = sprites.get(drawKey);
        }
    }
}