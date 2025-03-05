package omaloon.core;

import lombok.*;
import mindustry.*;
import mindustry.input.*;
import org.jetbrains.annotations.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OlInput{

    @NotNull
    public final InputHandler any;
    @Nullable
    public final DesktopInput desktop;
    @Nullable
    public final MobileInput mobile;

    public OlInput(@NonNull InputHandler any){
        this(
            any,
            Vars.mobile ? null : (DesktopInput)any,
            Vars.mobile ? (MobileInput)any : null
        );
    }

    public static void makeLazy(){
    }

}
