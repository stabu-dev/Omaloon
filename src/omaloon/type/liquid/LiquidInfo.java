package omaloon.type.liquid;

import lombok.*;
import lombok.experimental.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC,makeFinal = true)
public class LiquidInfo{
    float density,viscosity;
}
