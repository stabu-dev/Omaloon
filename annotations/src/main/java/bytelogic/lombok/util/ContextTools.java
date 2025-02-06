package bytelogic.lombok.util;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.util.*;
import lombok.*;
import lombok.experimental.*;
import lombok.javac.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ContextTools{
    JavacResolution resolution;
    Names names;
    Symtab symtab;
    ParserFactory parserFactory;

    public static ContextTools make(Context context){

        return new ContextTools(
        Util.resolution(context),
        Names.instance(context),
        Symtab.instance(context),
        ParserFactory.instance(context)
        );
    }

    public static ContextTools make(JavacNode typeNode){
        return make(typeNode.getContext());
    }
}
