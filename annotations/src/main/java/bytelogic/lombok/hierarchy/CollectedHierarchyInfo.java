package bytelogic.lombok.hierarchy;

import bytelogic.lombok.hierarchy.info.ClassInfo;
import bytelogic.lombok.hierarchy.info.InterfaceInfo;
import bytelogic.lombok.util.Util;
import lombok.javac.JavacNode;
import lombok.val;
import one.util.streamex.EntryStream;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class CollectedHierarchyInfo {
    public static final String JAVA_LANG_OBJECT = "java.lang.Object";
    public static final InterfaceInfo interfacesRoot = new InterfaceInfo("?interfacesRoot?", "?interfacesRoot?");
    static ClassInfo javaLangObject = new ClassInfo(JAVA_LANG_OBJECT, JAVA_LANG_OBJECT);
    static Map<String, ClassInfo> classInfoMap;
    static Map<String, InterfaceInfo> interfacesInfoMap;

    public static ClassInfo classInfo(String className) {
        return classInfoMap.get(className);
    }

    public static InterfaceInfo interfaceInfo(String name) {
        return interfacesInfoMap.get(name);
    }

    public static EntryStream<String, InterfaceInfo> interfaces() {
        return EntryStream.of(interfacesInfoMap)
                          .removeKeys(it -> it.equals(interfacesRoot.name));
    }

    public static EntryStream<String, ClassInfo> classes() {
        return EntryStream.of(classInfoMap);
    }


    @NotNull
    public static ArrayList<ClassInfo> collectAllImpl(InterfaceInfo interfaceInfo) {
        ArrayList<InterfaceInfo> allSubTypes = collectAllSub(interfaceInfo);
        val classInfoStream = allSubTypes
            .stream()
            .map(it -> it.impltypes.values())
            .flatMap(Collection::stream)
            .distinct()
            .toArray(ClassInfo[]::new);
        ;

        ArrayList<ClassInfo> implementedClasses = new ArrayList<>();
        Set<String> visitedNames = new HashSet<>();
        class Visitor {
            static void visit(Set<String> visitedNames, ClassInfo info, ArrayList<ClassInfo> targetCollection) {
                if (!visitedNames.add(info.name)) return;
                targetCollection.add(info);
                for (ClassInfo value : info.subtypes.values()) {
                    visit(visitedNames, value, targetCollection);
                }
            }
        }
        for (ClassInfo info : classInfoStream) {
            Visitor.visit(visitedNames, info, implementedClasses);
        }
        return implementedClasses;
    }

    @NotNull
    public static ArrayList<InterfaceInfo> collectAllSub(InterfaceInfo interfaceInfo) {
        ArrayList<InterfaceInfo> allSubTypes = new ArrayList<>();
        //noinspection unchecked
        BiConsumer<String, InterfaceInfo> visitor = new BiConsumer<>() {

            Set<String> visitedNames = new HashSet<>();

            @Override
            public void accept(String name, InterfaceInfo info) {
                if (!visitedNames.add(info.name)) return;
                allSubTypes.add(info);
                info.subtypes.forEach(this);
            }
        };
        visitor.accept(interfaceInfo.name, interfaceInfo);
        return allSubTypes;
    }

    public static ClassInfo classInfo(JavacNode typeNode) {
        return classInfo(Util.canonicalFullname(typeNode));
    }

    public static ClassInfo classInfoOrThrown(JavacNode typeNode) {
        String mirrorName = Util.canonicalFullname(typeNode);
        ClassInfo classInfo = classInfo(mirrorName);
        if (classInfo == null) throw new RuntimeException("Cannot find info for class " + mirrorName);
        return classInfo;
    }

    public static InterfaceInfo interfaceInfo(JavacNode typeNode) {
        return interfaceInfo(Util.canonicalFullname(typeNode));
    }

    public static InterfaceInfo interfaceInfoOrThrown(JavacNode typeNode) {
        String string = Util.canonicalFullname(typeNode);
        InterfaceInfo interfaceInfo = interfaceInfo(string);
        if (interfaceInfo == null) throw new RuntimeException("Cannot find info for interface " + string);
        return interfaceInfo;
    }
}
