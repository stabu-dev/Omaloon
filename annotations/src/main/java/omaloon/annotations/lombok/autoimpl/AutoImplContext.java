package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.util.GeneratedByVisitor;
import com.sun.tools.javac.tree.JCTree;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;

import java.util.Map;

@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PUBLIC)
public class AutoImplContext {
    Map<String, String> paramMap;
    JavacNode callerTypeNode;
    JavacNode callerMethodNode;
    JCTree.JCExpression producerNode;
    boolean justCreated;

    public GeneratedByVisitor generatedBy() {
        return new GeneratedByVisitor(callerTypeNode, producerNode);
    }
}
