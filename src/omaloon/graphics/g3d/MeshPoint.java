package omaloon.graphics.g3d;

import arc.math.geom.*;
import lombok.*;
import lombok.experimental.*;
import org.intellij.lang.annotations.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class MeshPoint{
    Vec3 position;
    float textureCordsX;
    float textureCordsY;
    @MagicConstant(valuesFromClass = MeshPoint.class)
    int radiusIndex;
    boolean nextAngle;
    public static final int innerRadius = 0;
    public static final int outerRadius = 0;
}
