package omaloon.annotations.lombok.autoimpl;

import bytelogic.lombok.hierarchy.*;
import bytelogic.lombok.hierarchy.info.*;
import bytelogic.lombok.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import lombok.*;
import lombok.core.*;
import lombok.javac.*;
import lombok.javac.handlers.*;
import omaloon.annotations.*;
import omaloon.annotations.lombok.autoimpl.info.*;
import one.util.streamex.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

import static bytelogic.lombok.hierarchy.CollectedHierarchyInfo.interfaces;
import static bytelogic.lombok.util.Util.canonicalFullname;

@HandlerPriority(value = -10, subValue = 20)
public class ImplProcessor extends JavacASTAdapter{
    private static boolean updated;
    @Nullable
    private Set<String> autoImplClasses;
    private TypeLibrary interfacesLibrary;
    private Map<InterfaceInfo, ArrayList<InterfaceInfo>> simpleInterfaceToAutoImpl;

    public static void setUpdated(){
        updated = true;
    }

    private static ArrayList<InterfaceInfo> flatInterfaceSuper(InterfaceInfo info){

        ArrayList<InterfaceInfo> flatten = new ArrayList<>();
        BiConsumer<String, InterfaceInfo> consumer = new BiConsumer<>(){
            Set<String> nameSet = new HashSet<>();

            @Override
            public void accept(String s, InterfaceInfo info){
                if(info == CollectedHierarchyInfo.interfacesRoot || !nameSet.add(s)) return;
                flatten.add(info);
                info.supertypes.forEach(this);

            }
        };
        consumer.accept(info.name, info);
        return flatten;
    }

    @NotNull
    private static ArrayList<String> findNoInject(JavacNode typeNode, JCTree.JCModifiers mods){
        ArrayList<String> list = new ArrayList<>();
        ContextLibrary classLibrary = ContextLibrary
            .ofClasses(typeNode)
            .withResolvedParts(null);

        for(JCTree.JCAnnotation annotation : mods.annotations){
            if(!annotation.annotationType.toString().endsWith("NoInject")) continue;
            AnnotationValues<AutoImplement.NoInject> values = JavacHandlerUtil.createAnnotation(AutoImplement.NoInject.class, typeNode.getNodeFor(annotation));
            String postfix = ".class";
            for(String classGuessDotClass : values.getRawExpressions("value")){
                if(!classGuessDotClass.endsWith(postfix)) continue;
                list.add(classLibrary.resolveFull(classGuessDotClass.substring(0, classGuessDotClass.length() - postfix.length())));
            }
        }
        return list;
    }

    @Override
    public void visitCompilationUnit(JavacNode top, JCTree.JCCompilationUnit unit){
        if(!updated) return;
        updated = false;

        interfacesLibrary = new TypeLibrary();
        interfaces()
            .values()
            .map(AbstractInfo::getFlatName)
            .forEach(interfacesLibrary::addType);

        interfacesLibrary.lock();

        autoImplClasses = interfaces()
            .values()
            .filter(ImplMarker::hasAutoImplData)
            .map(CollectedHierarchyInfo::collectAllImpl)
            .flatMap(StreamEx::of)
            .map(ClassInfo::getName)
            .toSet();
        simpleInterfaceToAutoImpl = interfaces()
            .values()

            //TODO  guess alphabetic order moment
            .mapToEntry(ImplProcessor::flatInterfaceSuper)
            .mapValues(it -> it
                .stream()
                .filter(ImplMarker::hasAutoImplData)
                .toList()
            ).filterValues(it -> !it.isEmpty())
            .mapValues(ArrayList::new)
            .toMap();

    }

    @Override
    public void visitType(JavacNode typeNode, JCTree.JCClassDecl type){
        if(autoImplClasses == null) return;
        if(typeNode.getElement() == null){

            return;
        }
        if(!autoImplClasses.contains(canonicalFullname(typeNode))) return;

        InfoAndPos[] interfaceInfos = StreamEx
            .ofNullable(type.implementing)
            .flatMap(StreamEx::of)
            .mapToEntry(Function.identity())
            .mapKeys(Object::toString)
            .filterKeys(string -> !string.contains("<")) //TODO Generic implement
            .mapKeys(it -> Paths.fullifyName(typeNode, it, interfacesLibrary))
            .mapKeys(CollectedHierarchyInfo::interfaceInfo)
            .nonNullKeys()
            .mapKeys(simpleInterfaceToAutoImpl::get)
            .nonNullKeys()
            .flatMapKeys(StreamEx::of)
            .distinctKeys()
            .mapKeyValue(InfoAndPos::new)
            .toArray(InfoAndPos[]::new);

        for(InfoAndPos info : interfaceInfos){

            if(findNoInject(typeNode, type.mods).contains(info.info.name)) continue;
            implement(info.info, info.producer, typeNode, type);
        }

    }

    private void implement(InterfaceInfo info, @NonNull JCTree.JCExpression producer, JavacNode typeNode, JCTree.JCClassDecl type){
        AutoImplInformation information = info.get(ImplMarker.isAutoImpl);
        Map<AST.Kind, List<JavacNode>> childrenMap = StreamEx
            .of(typeNode.down().iterator())
            .mapToEntry(LombokNode::getKind, Function.identity())
            .sortedBy(Map.Entry::getKey)
            .collapseKeys()
            .toMap();
        Map<String, JavacNode> fields = StreamEx
            .of(childrenMap.getOrDefault(AST.Kind.FIELD, Collections.emptyList()))
            .toMap(JavacNode::getName, Function.identity());
        for(Map.Entry<String, AutoImplInformation.FieldInfo> entry : information.fields.entrySet()){
            JavacNode existed = fields.get(entry.getKey());
            AutoImplInformation.FieldInfo fieldInfo = entry.getValue();
            if(existed != null){
                if(existed.hasAnnotation(AutoImplement.NoInject.class)){
                    continue;
                }
                existed.addError("Cannot insert field %s.%s. Field with this name already exists.".formatted(
                    fieldInfo.intefaceName, fieldInfo.name
                ));
                continue;
            }
            JavacTreeMaker m = typeNode.getTreeMaker();
            TreeMaker imaker = m.getUnderlyingTreeMaker();

            JavacAST ast = typeNode.getAst();


            TreeCopier<Object> copier = new TreeCopier<>(imaker);

            JCTree.JCVariableDecl decl = TypeInliner.copyWithInlineTypes(fieldInfo.fieldNode);
            JCTree.JCVariableDecl newChild = m.VarDef(
                m.Modifiers(decl.mods.flags & ~Flags.STATIC),
                ast.toName(fieldInfo.name),
                copier.copy(decl.vartype),
                copier.copy(decl.init)
            );
            type.defs = type.defs.append(newChild);
            typeNode.add(
                newChild,
                AST.Kind.FIELD);
        }

        Map<String, JavacNode> methods = EntryStream
            .of(childrenMap.get(AST.Kind.METHOD))
            .values()
            .mapToEntry(it -> Util.methodDesc(it, (JCTree.JCMethodDecl)it.get()), Function.identity())
            .toMap();


        for(Map.Entry<String, MethodInfo> entry : information.methods.entrySet()){
            String methodDecs = entry.getKey();
            MethodInfo methodInfo = entry.getValue();
            JavacNode existed = methods.get(methodDecs);

            JCTree.JCMethodDecl decl;


            AutoImplContext.AutoImplContextBuilder contextBuilder = AutoImplContext
                .builder()
                .callerTypeNode(typeNode)
                .producerNode(producer);
            if(existed == null){
                contextBuilder
                    .paramMap(collectParams(typeNode))
                    .justCreated(true);
                decl = methodInfo.make(contextBuilder.build());
                JavacHandlerUtil.injectMethod(typeNode, decl);
                JavacNode nodeFor = typeNode.getNodeFor(decl);
                methods.put(methodDecs, nodeFor);
                contextBuilder.callerMethodNode(nodeFor);
            }else{
                contextBuilder
                    .paramMap(collectParams(existed))
                    .callerMethodNode(existed)
                    .justCreated(false);
                decl = (JCTree.JCMethodDecl)existed.get();
                if(findNoInject(existed, decl.mods).contains(info.name)) continue;
            }
            methodInfo.join(decl, contextBuilder.build());

        }
    }

    private Map<String, String> collectParams(JavacNode typeNode){
        JavacNode node = typeNode;
        Map<String, String> map = new HashMap<>();
        JavacAST ast = typeNode.getAst();
        while(node != null){
            JCTree.JCModifiers mods =
                switch(typeNode.getKind()){
                    case TYPE -> ((JCTree.JCClassDecl)typeNode.get()).mods;
                    case FIELD -> ((JCTree.JCVariableDecl)typeNode.get()).mods;
                    case METHOD -> ((JCTree.JCMethodDecl)typeNode.get()).mods;
                    case COMPILATION_UNIT, INITIALIZER, ANNOTATION, ARGUMENT, LOCAL, STATEMENT, TYPE_USE -> null;
                };
            if(mods != null){

                for(JCTree.JCAnnotation annotation : mods.annotations){
                    JavacNode annotationNode = ast.get(annotation);
                    if(!JavacHandlerUtil.annotationTypeMatches(AutoImplement.Util.SetParam.class, annotationNode))
                        continue;
                    AutoImplement.Util.SetParam setParam = AnnotationValues.of(AutoImplement.Util.SetParam.class, annotationNode).getInstance();
                    map.put(setParam.name(), setParam.value());
                }
            }
            node = node.up();
        }
        return map;
    }

    @AllArgsConstructor
    static class InfoAndPos{
        InterfaceInfo info;
        JCTree.JCExpression producer;
    }
}
