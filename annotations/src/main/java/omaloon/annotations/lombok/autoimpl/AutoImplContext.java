package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.util.*;
import com.sun.tools.javac.tree.*;
import lombok.*;
import lombok.experimental.*;
import lombok.javac.*;

import java.util.*;

@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PUBLIC)
public class AutoImplContext{
    Map<String, String> paramMap;
    JavacNode callerTypeNode;
    JavacNode callerMethodNode;
    JCTree.JCExpression producerNode;
    boolean justCreated;

    public GeneratedByVisitor generatedBy(){
        return new GeneratedByVisitor(callerTypeNode, producerNode);
    }
}
