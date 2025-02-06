package bytelogic.lombok.util;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.*;
import lombok.*;
import lombok.javac.*;

@AllArgsConstructor
public class GeneratedByVisitor extends TreeScanner<Void, Void>{
    public final JavacNode contextNode;
    public final JCTree sourceNode;

    @Override
    public Void scan(Tree tree, Void unused){
        Util.setGeneratedBy((JCTree)tree, contextNode, sourceNode);
        return super.scan(tree, unused);
    }
}
