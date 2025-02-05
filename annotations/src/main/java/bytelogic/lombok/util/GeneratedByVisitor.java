package bytelogic.lombok.util;

import com.github.javaparser.ast.visitor.TreeVisitor;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import lombok.AllArgsConstructor;
import lombok.javac.JavacNode;

@AllArgsConstructor
public class GeneratedByVisitor extends TreeScanner<Void,Void> {
    public final JavacNode contextNode;
    public final JCTree sourceNode;

    @Override
    public Void scan(Tree tree, Void unused) {
        Util.setGeneratedBy((JCTree) tree, contextNode, sourceNode);
        return super.scan(tree, unused);
    }
}
