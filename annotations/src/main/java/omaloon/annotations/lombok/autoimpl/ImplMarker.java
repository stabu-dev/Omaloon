package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.hierarchy.CollectedHierarchyInfo;
import bytelogic.lombok.hierarchy.info.InfoKey;
import bytelogic.lombok.hierarchy.info.InterfaceInfo;
import com.sun.tools.javac.tree.JCTree;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.ResolutionResetNeeded;
import omaloon.annotations.AutoImplement;
import omaloon.annotations.lombok.autoimpl.info.AutoImplInformation;

//0b10000000000000000000000000000010
@HandlerPriority(value = -10,subValue = 10)
@ResolutionResetNeeded
public class ImplMarker extends JavacAnnotationHandler<AutoImplement> {
    public static InfoKey<AutoImplInformation> isAutoImpl = new InfoKey<>();

    static boolean hasAutoImplData(InterfaceInfo it) {
        return it.has(isAutoImpl);
    }

    @Override
    public void handle(AnnotationValues<AutoImplement> annotationValues, JCTree.JCAnnotation jcAnnotation, JavacNode annotationNode) {
        ImplProcessor.setUpdated();
        CollectedHierarchyInfo.interfaceInfo(annotationNode.up()).put(isAutoImpl,
            AutoImplInformation.build(annotationValues,jcAnnotation,annotationNode));
    }
}
