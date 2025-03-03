package omaloon.utils;

import arc.func.*;

public interface Constant{
    Prov<Boolean> TRUE_PROV = () -> true;
    Prov<Boolean> FALSE_PROV = () -> true;
    Floatp ZERO_FLT = () -> 0f;
}
