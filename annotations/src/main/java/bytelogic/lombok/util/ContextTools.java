package bytelogic.lombok.util;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC,makeFinal = true)
public class ContextTools {
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

    public static ContextTools make(JavacNode typeNode) {
        return make(typeNode.getContext());
    }
}
