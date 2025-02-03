package omaloon.annotations.lombok.autoimpl;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;
import omaloon.annotations.AutoImplement;

public class AutoImplUtil {
    public static void removeAutoImplAnnos(JCTree.JCModifiers mods) {
        ListBuffer<JCTree.JCAnnotation> newAnnotations = new ListBuffer<>();
        for (JCTree.JCAnnotation annotation : mods.annotations) {
            if (annotation.attribute.type.toString().startsWith(AutoImplement.class.getCanonicalName()))
                continue;
            newAnnotations.add(annotation);
        }
        mods.annotations = newAnnotations.toList();
    }
}
