package omaloon.type.weather;

import arc.math.Mathf;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.WeatherState;

import java.util.Arrays;
import java.util.Comparator;

public class BulletWeather extends SpawnerWeather {
    public BulletStack[] bullets;
    public float bulletChange = 0.2f;
    public Team bulletTeam = Team.derelict;

    public BulletWeather(String name) { super(name);}

    @Override
    public void spawnAt(WeatherState state, float x, float y) {
        BulletType b = getBullet();

        b.create(null, bulletTeam, x, y, 0f);
    }

    @Override
    public boolean canSpawn(WeatherState state){
        return Mathf.randomBoolean(bulletChange * state.intensity);
    }

    public BulletType getBullet(){
        for (int i = 0; i < bullets.length; i++) {
            var item = (BulletStack) bullets[i];

            if (Mathf.random() < item.change){
                return item.bullet;
            }
        }

        return bullets[bullets.length-1].bullet;
    }

    public void setBullets(Object... items){
        var stack = new BulletStack[items.length/2];

        for (int i = 0; i < items.length; i += 2) {
            stack[i/2] = new BulletStack((BulletType) items[i], (float) items[i + 1]);
        }

        Arrays.sort(stack, new Comparator<BulletStack>() {
            @Override
            public int compare(BulletStack o1, BulletStack o2) {
                if (o1.change == o2.change) return 0;
                return o1.change > o2.change ? 1 : -1;
            }
        });

        bullets = stack;
    }

    public static class BulletStack {
        public BulletType bullet;
        public float change;

        public BulletStack(BulletType bullet, float change){
            this.bullet = bullet;
            this.change = change;
        }

    }

}
