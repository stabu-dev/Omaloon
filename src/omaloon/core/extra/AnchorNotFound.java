package omaloon.core.extra;

import arc.*;
import omaloon.core.extra.RelatedApplicationListener.*;

public class AnchorNotFound extends Exception{
    public AnchorNotFound(ApplicationListener applicationListener, RelativeOrder relativeOrder, ApplicationListener anchor){
        super(String.format(
            "Cannot find '%s' to add listener '%s' with order '%s'",
            anchor.toString(),
            applicationListener.toString(),
            relativeOrder.toString()
        ));
    }
}
