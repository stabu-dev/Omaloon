package omaloon.annotations.lombok.autoimpl.info;

import asmlib.lombok.javaparser.CompileBodyVisitor;
import bytelogic.lombok.util.ContextLibrary;
import bytelogic.lombok.util.GeneratedByVisitor;
import bytelogic.lombok.util.Util;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import lombok.*;
import lombok.javac.JavacAST;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import omaloon.annotations.AutoImplement;
import omaloon.annotations.AutoImplement.Inject.InjectPosition;
import omaloon.annotations.lombok.autoimpl.AutoImplContext;
import omaloon.annotations.lombok.autoimpl.AutoImplUtil;
import omaloon.annotations.lombok.autoimpl.Paths;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static bytelogic.lombok.util.ContextLibrary.makeLib;
import static one.util.streamex.StreamEx.of;

@AllArgsConstructor
@Getter
public class MethodInfo {
    public final String interfaceFullName;
    public final JavacNode typeNode;

    final MethodDeclarationInfo info;
    final JavacResolution resolution;
    final ParserFactory parserFactory;

    final CompileBodyVisitor transformer;
    @Nullable
    AutoImplement.Inject inject;

    public static MethodInfo create(JavacNode typeNode, JavacNode methodNode, AutoImplement.Inject inject) {
        MethodDeclarationInfo info = MethodDeclarationInfo.make(methodNode);
        return new MethodInfo(
            Util.canonicalFullname(typeNode),
            typeNode,
            info,
            info.contextLibrary.resolution,
            ParserFactory.instance(typeNode.getContext()),
            Util.transformer(methodNode),
            inject
        );
    }

    @Nullable
    public static String extractString(JCTree.JCExpression jcExpression, JavacAST ast, String desc) {
        String string;
        if (!(jcExpression instanceof JCTree.JCLiteral literal)) {
            ast.get(jcExpression).addError("Only Literals allowed in " + desc);
            string = null;

        } else {
            string = literal.value.toString();
        }
        return string;
    }

    private boolean tryInjectPosition(JCTree.JCMethodDecl decl, AutoImplContext context, List<JCTree.JCStatement> newCode) {


        boolean[] hasInject = {false};
        ParserFactory parserFactory = ParserFactory.instance(context.callerMethodNode.getContext());
        TreeCopier<Object> copier = new TreeCopier<>(transformer.imaker) {
            @Override
            public <T extends JCTree> T copy(T tree, Object o) {
                super.copy(tree, o);
                return tree;
            }

            @Override
            public <T extends JCTree> List<T> copy(List<T> trees, Object o) {
                if (trees.isEmpty() || trees.stream().filter(it -> it instanceof JCTree.JCStatement).count() != trees.length())
                    return trees.map(this::copy);
                //noinspection unchecked
                return (List<T>) List.from(of((List<? extends JCTree.JCStatement>) trees)
                    .limit(trees.length())
                    .flatCollection(it -> {
                        if (!hasInject(context, it)) return List.of(copy(it));
                        hasInject[0] = true;
                        StringWriter writer = new StringWriter();
                        try {
                            new InlineApplyPrinter(writer, Paths.INJECT_METHOD, makeLib(context.callerMethodNode, Paths.library), (printer, tree) -> {
                                printer.printStats(newCode);

                            }).printExpr(it);
                        } catch (IOException e) {
                            throw Lombok.sneakyThrow(e);
                        }
                        String input = "{\n" + writer + "\n}";
                        JavacParser parser = parserFactory.newParser(input, true, true, true, false);
                        return ((JCTree.JCBlock) parser.parseStatement()).stats;
                    })
                    .toArray(JCTree.JCStatement[]::new)
                );
            }
        };
        decl.body.stats = copier.copy(decl.body.stats);
        if (hasInject[0]) {
            context.callerMethodNode.getAst().setChanged();
        }
        return hasInject[0];
    }

    private boolean hasInject(AutoImplContext context, JCTree tree) {
        boolean[] hasInject = {false};

        tree.accept(new TreeScanner() {
            @Override
            public void scan(JCTree tree) {
                if (!hasInject[0]) super.scan(tree);
            }

            @Override
            public void scan(List<? extends JCTree> trees) {
                //TODO mayby just filter
            }

            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {

                JavacNode tmpNode = context.callerMethodNode;

                if (!Paths.expressionMatch(tmpNode, tree.meth.toString(), Paths.INJECT_METHOD)) {
                    super.visitApply(tree);
                    return;
                }
                ContextLibrary library = ContextLibrary.ofClasses(tmpNode);

                JCTree.JCFieldAccess access = (JCTree.JCFieldAccess) tree.args.get(0);
                String innerClass = library.className(access.selected);
                hasInject[0] |= interfaceFullName.equals(innerClass);
            }
        });
        return hasInject[0];
    }

    public JCTree.JCMethodDecl make(@NonNull AutoImplContext rawContext) {

        JCTree.JCMethodDecl declaration = TypeInliner.copyWithInlineTypes(info.node);
        AutoImplUtil.removeAutoImplAnnos(declaration.mods);
        declaration.mods.flags &= ~Flags.DEFAULT;
        declaration.mods.flags |= Flags.PUBLIC;


        if (inject != null && inject.shouldAddSuper()) {
            var m = transformer.maker;
            m.at(rawContext.producerNode.getPreferredPosition());
            Names names = Names.instance(typeNode.getContext());
            declaration.body = m.Block(0, List.of(
                m.Exec(
                    m.Apply(
                        declaration.typarams
                            .map(JCTree.JCTypeParameter::getName)
                            .map(m::Ident),
                        m.Select(m.Ident(names._super), declaration.name),
                        declaration.params.map(JCTree.JCVariableDecl::getName)
                                          .map(m::Ident)
                    )
                )
            ));
        } else {
            declaration.body.stats = List.nil();
        }
        rawContext.generatedBy().scan(declaration, null);
        return declaration;
    }

    public void join(JCTree.JCMethodDecl existed, AutoImplContext context) {

        JCTree.JCBlock body = existed.body;
        List<JCTree.JCStatement> preprocessed = prepareStats(context);
        if (tryInjectPosition(existed, context, preprocessed)) {
            return;
        }
        InjectPosition position;
        if (inject == null) {
            //TODO
            position = InjectPosition.Head;
        } else {
            position = inject.value();
        }

        Void nil = switch (position) {
            case Head -> {
                body.stats = body.stats.prependList(preprocessed);
                yield null;
            }
            case Tail -> {
                body.stats = body.stats.appendList(preprocessed);

                yield null;
            }
            case AfterSuper, BeforeSuper -> {
                final JavacResolution resolution = getResolution();
                final ContextLibrary library = info.contextLibrary;
                final String originalParams = of(info.decl.getParameters())
                    .map(JCTree.JCVariableDecl::getType)
                    .map(library::className)
                    .joining(";");
                TreeCopier<Object> simpleCopier = new TreeCopier<>(transformer.imaker);
                boolean[] foundSuper = {false};
                TreeCopier<Object> copier = new TreeCopier<>(transformer.imaker) {


                    @SuppressWarnings("rawtypes")
                    @Override
                    public <T extends JCTree> List<T> copy(List<T> trees, Object object) {

                        Function tCollectionFunction = (Function<JCTree, Collection<?>>) it__ -> {
                            if (!(it__ instanceof JCTree.JCExpressionStatement it)) return List.of(it__);
                            JCTree.JCExpressionStatement it__Copy = copy(it, object);
                            if (!(it.expr instanceof JCTree.JCMethodInvocation apply)) return List.of(it__Copy);
                            String postfix = "super." + info.name;
                            //TODO better super handling
                            if (!apply.meth.toString().equals(postfix) && !apply.meth.toString().endsWith('.' + postfix))
                                return List.of(it__Copy);

                            JavacNode localContext = context.callerMethodNode.getNodeFor(it);


                            Map<JCTree, JCTree> resolved = resolution.resolveMethodMember(localContext);
                            String resolvedArguments = of(apply.getArguments())
                                .map(resolved::get)
                                .map(it_ -> it_.type)
                                .joining(";");
                            if (!originalParams.equals(resolvedArguments)) return List.of(it__Copy);
                            foundSuper[0] = true;
                            if (position == InjectPosition.BeforeSuper) {
                                return simpleCopier.copy(preprocessed).append(it__Copy);
                            } else {
                                return simpleCopier.copy(preprocessed).prepend(it__Copy);
                            }
                        };
                        //noinspection unchecked
                        return List.from(of(trees)
                            .flatCollection(tCollectionFunction)


                        );
                    }
                };
                existed.body.stats = copier.copy(existed.body.stats);

                if (!foundSuper[0]) {
                    context.callerMethodNode
                        .addError("Cannot find invoking super method to inject implementation from " + interfaceFullName);
                }
                yield null;
            }
        };
        //noinspection ConstantValue
        if (false) System.out.println(nil);
        context.callerMethodNode.getAst().setChanged();
        context.callerMethodNode.rebuild();
    }

    @SneakyThrows
    private List<JCTree.JCStatement> preprocess(List<JCTree.JCStatement> stats, AutoImplContext context) {


        JCTree.JCStatement[] array = stats.toArray(JCTree.JCStatement[]::new);
        ParserFactory parserFactory = ParserFactory.instance(info.node.getContext());
        GeneratedByVisitor generatedByMarker = context.generatedBy();
        for (int i = 0; i < array.length; i++) {

            JCTree.JCStatement jcStatement = array[i];
            StringWriter s = new StringWriter();
            new InlineApplyPrinter(s, Paths.PARAM_METHOD, makeLib(info.node, Paths.library), (printer, tree) -> {
                JavacAST ast = info.node
                    .getAst();

                String paramName = extractString(tree.args.get(0), ast, Paths.PARAM_METHOD);
                String defaultExpression = extractString(tree.args.get(1), ast, Paths.PARAM_METHOD);

                String expression = context.paramMap.getOrDefault(paramName, defaultExpression);

                printer.print(expression);

            }) {
                @SneakyThrows
                boolean tryFixName(JCTree.JCIdent tree) {
                    if (!(tree.sym instanceof Symbol.VarSymbol varSymbol)) return false;
                    if (!(varSymbol.owner instanceof Symbol.MethodSymbol methodSymbol)) return false;
                    if (!(methodSymbol.owner instanceof Symbol.ClassSymbol classSymbol)) return false;
                    if (!classSymbol.className().equals(interfaceFullName)) return false;
                    if (!methodSymbol.toString().equals(info.symbolString)) return false;
                    JCTree.JCMethodDecl newDecl = (JCTree.JCMethodDecl) context.callerMethodNode.get();
                    int varIndex = methodSymbol.params.indexOf(varSymbol);
                    print(newDecl.params.get(varIndex).getName().toString());
                    return true;
                }

                @Override
                public void visitIdent(JCTree.JCIdent tree) {

                    if (tryFixName(tree)) return;
                    super.visitIdent(tree);
                }
            }.printStat(jcStatement);
            String stringify = s.toString();


            JavacParser javacParser = parserFactory.newParser(stringify, false, false, false, false);

            array[i] = javacParser.parseStatement();
            generatedByMarker.scan(array[i], null);
        }
        return List.from(array);
    }

    public List<JCTree.JCStatement> prepareStats(AutoImplContext context) {
        info.decl.body = TypeInliner.copyWithInlineTypes(info.node, info.decl.body);

        JCTree.JCMethodDecl decl = Util.resolveSym(info.node);
        return preprocess(decl.body.stats, context);
    }

}
