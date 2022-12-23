package ol.utils;

import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;

public class OlPlans {
    private static Eachable<BuildPlan> list;
    private static BuildPlan plan;

    public static void set(BuildPlan plan, Eachable<BuildPlan> list) {
        OlPlans.list = list;
        OlPlans.plan = plan;
    }

    public static BuildPlan get(int x, int y) {
        if(OlPlans.plan == null) {
            return null;
        }

        BuildPlan[] result = new BuildPlan[1];
        boolean[] found = { false };

        OlPlans.list.each(buildPlan -> {
            if(buildPlan == OlPlans.plan && !found[0]) {
                OlPlans.list.each(plan -> {
                    if(plan == OlPlans.plan) {
                        return;
                    }

                    if(buildPlan.x - plan.x == -x) {
                        if(buildPlan.y - plan.y == -y) {
                            result[0] = plan;
                        }
                    }
                });

                found[0] = true;
            }
        });

        return result[0];
    }
}