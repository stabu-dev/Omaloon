package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.hierarchy.*;
import bytelogic.lombok.hierarchy.info.*;
import com.sun.tools.javac.tree.*;
import lombok.core.*;
import lombok.javac.*;
import omaloon.annotations.*;
import omaloon.annotations.lombok.autoimpl.info.*;

//0b10000000000000000000000000000010
@HandlerPriority(value = -10, subValue = 10)
@ResolutionResetNeeded
public class ImplMarker extends JavacAnnotationHandler<AutoImplement>{
    public static InfoKey<AutoImplInformation> isAutoImpl = new InfoKey<>();

    static boolean hasAutoImplData(InterfaceInfo it){
        return it.has(isAutoImpl);
    }

    @Override
    public void handle(AnnotationValues<AutoImplement> annotationValues, JCTree.JCAnnotation jcAnnotation, JavacNode annotationNode){
        ImplProcessor.setUpdated();
        CollectedHierarchyInfo.interfaceInfo(annotationNode.up()).put(isAutoImpl,
        AutoImplInformation.build(annotationValues, jcAnnotation, annotationNode));
    }
}
