package omaloon.entities.abilities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import omaloon.gen.Chainedc;

public class ConnectChainAbility extends Ability {
    public float connectTime = 60f;
    public float connectAngle = 30f;

    public float findRadius = 4;

    public float pullStrength = 0.1f;

    @Override
    public void draw(Unit unit) {
        Draw.color(Pal.accent);
        Draw.alpha(Mathf.clamp(data / connectTime));

        var connectors = getConnectors(unit);
        Fill.circle(Tmp.v1.x, Tmp.v1.y, findRadius);
        if (!connectors.isEmpty()) Fill.circle(connectors.first().x, connectors.first().y, findRadius);
    }

    public Seq<Unit> getConnectors(Unit unit) {
        if (!(unit instanceof Chainedc chain)) return Seq.with();
        Tmp.v1.trns(unit.rotation, unit.hitSize + findRadius).add(unit);
        return Groups.unit.intersect(Tmp.v1.x - findRadius, Tmp.v1.y - findRadius, findRadius * 2, findRadius * 2)
        .retainAll(
            u -> u instanceof Chainedc other &&
            other.tail() == u &&
            other.head() != chain.head() &&
            u.type == unit.type &&
            Angles.within(unit.rotation, u.rotation, connectAngle)
        );
    }

    @Override
    public void init(UnitType type) {
        if (findRadius < 0) findRadius = type.hitSize;
    }

    // TODO annotation Call generator for proper multiplayer compatibility
    @Override
    public void update(Unit unit) {
        if (!(unit instanceof Chainedc chain)) throw new RuntimeException("Unit is not an instance of Chainedc. Do not use this ability.");

        if (chain.head() != unit) return;

        var connectors = getConnectors(unit);
        if (!connectors.isEmpty()) {
            Chainedc other = ((Chainedc) connectors.first());

            data += Time.delta;

            if (data > connectTime) {
                other.connect(unit);
                data = 0f;
            }

            Tmp.v1.trns(other.rotation() + 180f, other.type().segmentSpacing).add(other).sub(unit).limit(pullStrength);
//            Tmp.v2.set(Tmp.v1).sub(unit).limit(pullStrength);
            unit.move(Tmp.v1);
        } else {
            data = Mathf.approachDelta(data, 0f, connectTime / 60f);
        }
    }
}
