package omaloon.annotations.processors.impl;

import arc.*;
import arc.audio.*;
import arc.files.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import com.squareup.javapoet.*;

import mindustry.*;
import mindustry.ui.*;

import omaloon.annotations.processors.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;

/** @author GlennFolker */
@SuppressWarnings("unused")
@SupportedAnnotationTypes("java.lang.Override")
public class AssetsProcessor extends BaseProcessor{
    Seq<Asset> assets = new Seq<>();

    {
        rounds = 2;
    }

    @Override
    public void process(RoundEnvironment roundEnv) throws Exception{
        if(round == 1){
            assets.clear().addAll(
            new Asset(){
                @Override
                public TypeElement type(){
                    return toType(Sound.class);
                }

                @Override
                public String directory(){
                    return "sounds";
                }

                @Override
                public String name(){
                    return classPrefix + "Sounds";
                }

                @Override
                public boolean valid(Fi file){
                    return file.extEquals("ogg") || file.extEquals("mp3");
                }

                @Override
                public void load(MethodSpec.Builder builder){
                    builder.addStatement("return $T.tree.loadSound(name)", cName(Vars.class));
                }
            },
            new Asset(){
                @Override
                public TypeElement type(){
                    return toType(Music.class);
                }

                @Override
                public String directory(){
                    return "music";
                }

                @Override
                public String name(){
                    return classPrefix + "Musics";
                }

                @Override
                public boolean valid(Fi file){
                    return file.extEquals("ogg") || file.extEquals("mp3");
                }

                @Override
                public void load(MethodSpec.Builder builder){
                    builder.addStatement("return $T.tree.loadMusic(name)", cName(Vars.class));
                }
            }
            );
        }else if(round == 2){
            for(Asset a : assets){
                TypeElement type = a.type();

                TypeSpec.Builder spec = TypeSpec.classBuilder(a.name()).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(
                MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T()", cName(AssertionError.class))
                .build()
                );

                MethodSpec.Builder specLoad = MethodSpec.methodBuilder("load").addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                .returns(tName(type))
                .addParameter(cName(String.class), "name");

                a.load(specLoad);
                spec.addMethod(specLoad.build());

                MethodSpec.Builder globalLoad = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addStatement("if($T.headless) return", cName(Vars.class));

                boolean useProp = a.properties();

                Fi propFile = rootDir.child("main/assets/" + a.directory() + "/" + a.propertyFile());
                Log.info("Asset properties file path: " + "main/assets/" + a.directory() + "/" + a.propertyFile());
                ObjectMap<String, String> temp = null;
                if(useProp && propFile.exists()){
                    PropertiesUtils.load(temp = new ObjectMap<>(), propFile.reader());
                }else if(useProp && !propFile.exists()){
                    Log.warn("Property file not found: @", propFile.path());
                }

                ObjectMap<String, String> properties = temp;

                String dir = "main/assets/" + a.directory();
                rootDir.child(dir).walk(path -> {
                    if(path.isDirectory() || (a.properties() && path.equals(propFile)) || !a.valid(path)) return;

                    String p = path.absolutePath();
                    String relativePathFromAssetDir;
                    String dirAbsolutePath = rootDir.child(dir).absolutePath();

                    if(p.startsWith(dirAbsolutePath)){
                        relativePathFromAssetDir = p.substring(dirAbsolutePath.length() + (p.charAt(dirAbsolutePath.length()) == '/' || p.charAt(dirAbsolutePath.length()) == '\\' ? 1 : 0));
                    }else{
                        relativePathFromAssetDir = path.name();
                    }

                    String fieldName = Strings.kebabToCamel(path.nameWithoutExtension());
                    String stripped = relativePathFromAssetDir.substring(0, relativePathFromAssetDir.length() - (path.extension().length() + 1));

                    spec.addField(
                    FieldSpec.builder(tName(type), fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .initializer(a.initializer())
                    .build()
                    );

                    globalLoad.addStatement("$L = load($S)", fieldName, stripped);

                    if(a.properties() && properties != null){
                        Seq<String> props = properties.keys().toSeq().select(propKey -> propKey.startsWith(stripped + "."));
                        for(String prop : props){
                            String[] parts = prop.split("\\.", 2);
                            if(parts.length < 2) continue;

                            String field = prop.substring(stripped.length() + 1);
                            String val = properties.get(prop);

                            if(!val.startsWith("[")){
                                globalLoad.addStatement("$L.$L = $L", fieldName, field, val);
                            }else{
                                Seq<String> rawargs = Seq.with(val.substring(1, val.length() - 1).split("\\s*,\\s*"));
                                String format = rawargs.remove(0);

                                Seq<Object> args = rawargs.map(arg -> {
                                    if(arg.matches("-?\\d+")) return Integer.parseInt(arg);
                                    if(arg.matches("-?\\d*\\.\\d+([eE][-+]?\\d+)?")) return Float.parseFloat(arg);
                                    if(arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) return Boolean.parseBoolean(arg);
                                    return arg;
                                });
                                args.insert(0, fieldName);
                                args.insert(1, field);

                                globalLoad.addStatement("$L.$L = " + format, args.toArray());
                            }
                        }
                    }
                });

                spec.addMethod(globalLoad.build());
                write(spec.build());
            }

            TypeSpec.Builder loaderBuilder = TypeSpec.classBuilder(classPrefix + "IconLoader")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            // loadIcons method
            MethodSpec.Builder loadIconsMethod = MethodSpec.methodBuilder("loadIcons")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("$T iconProperties = new $T()", cName(java.util.Properties.class), cName(java.util.Properties.class))
            .beginControlFlow("try($T reader = $T.tree.get(\"icons/\" + $S + \"-icons.properties\").reader(512))", cName(java.io.Reader.class), cName(Vars.class), modName)
            .addStatement("iconProperties.load(reader)")
            .nextControlFlow("catch($T e)", cName(Exception.class))
            .addStatement("return")
            .endControlFlow()
            .beginControlFlow("for($T.Entry<Object, Object> entry : iconProperties.entrySet())", cName(java.util.Map.class))
            .addStatement("String codePointStr = (String)entry.getKey()")
            .addStatement("String[] valueParts = ((String)entry.getValue()).split(\"[|]\")")
            .addStatement("if(valueParts.length < 2) continue")
            .beginControlFlow("try")
            .addStatement("int codePoint = Integer.parseInt(codePointStr)")
            .addStatement("String contentName = valueParts[0]")
            .addStatement("String textureName = valueParts[1]")
            .addStatement("$T region = $T.atlas.find(textureName)", cName(TextureRegion.class), cName(Core.class))
            .addStatement("$T.registerIcon(contentName, textureName, codePoint, region)", cName(Fonts.class))
            .addStatement("$T iconFont = $T.icon", cName(Font.class), cName(Fonts.class))
            .addStatement("int size = (int)(iconFont.getData().lineHeight / iconFont.getData().scaleY)")
            .addStatement("$T out = $T.fit.apply(region.width, region.height, size, size)", cName(arc.math.geom.Vec2.class), cName(Scaling.class))
            .addStatement("$T glyph = new $T()", cName(Font.Glyph.class), cName(Font.Glyph.class))
            .addStatement("glyph.id = codePoint")
            .addStatement("glyph.srcX = 0")
            .addStatement("glyph.srcY = 0")
            .addStatement("glyph.width = (int)out.x")
            .addStatement("glyph.height = (int)out.y")
            .addStatement("glyph.u = region.u")
            .addStatement("glyph.v = region.v2")
            .addStatement("glyph.u2 = region.u2")
            .addStatement("glyph.v2 = region.v")
            .addStatement("glyph.xoffset = 0")
            .addStatement("glyph.yoffset = -size")
            .addStatement("glyph.xadvance = size")
            .addStatement("glyph.kerning = null")
            .addStatement("glyph.fixedWidth = true")
            .addStatement("glyph.page = 0")
            .addStatement("iconFont.getData().setGlyph(codePoint, glyph)")

            .nextControlFlow("catch($T ignored)", cName(Exception.class))
            .endControlFlow()
            .endControlFlow();
            loaderBuilder.addMethod(loadIconsMethod.build());

            write(loaderBuilder.build());

            TypeSpec.Builder iconcBuilder = TypeSpec.classBuilder(classPrefix + "Iconc")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            ObjectMap<String, String> iconMap = new OrderedMap<>();
            Fi iconPropFile = rootDir.child("main/assets/icons/" + modName + "-icons.properties");
            if(iconPropFile.exists()){
                PropertiesUtils.load(iconMap, iconPropFile.reader());
            }

            StringBuilder iconcAll = new StringBuilder();
            CodeBlock.Builder iconcStatic = CodeBlock.builder();

            iconcBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ObjectIntMap.class, String.class),
            "codes", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("new ObjectIntMap<>()").build());

            iconcBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(IntMap.class, String.class),
            "codeToName", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).initializer("new IntMap<>()").build());

            iconMap.each((key, val) -> {
                String[] split = val.split("\\|");
                if(split.length < 2) return;

                String contentName = split[0];
                int code = Integer.parseInt(key);
                String name = Strings.kebabToCamel(contentName);

                if(javax.lang.model.SourceVersion.isKeyword(name)) name += "s";

                iconcAll.append((char)code);

                iconcBuilder.addField(FieldSpec.builder(char.class, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc(String.format("\\u%04x", code))
                .initializer("'" + ((char)code) + "'").build());

                iconcStatic.addStatement("codes.put($S, $L)", name, code);
                iconcStatic.addStatement("codeToName.put($L, $S)", code, name);
            });

            iconcBuilder.addField(FieldSpec.builder(String.class, "all", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", iconcAll.toString()).build());

            iconcBuilder.addStaticBlock(iconcStatic.build());

            write(iconcBuilder.build());
        }
    }

    interface Asset{
        /** @return The type of the asset */
        TypeElement type();

        /** @return The asset directory must not be surrounded with {@code /} */
        String directory();

        /** @return The class name */
        String name();

        /** @return Whether to apply custom properties to the asset */
        default boolean properties(){
            return false;
        }

        /**
         * @return The property tile, looked up if {@link #properties()} is true. This file's path is relative to
         * {@link #directory()} and must not be surrounded with {@code /}
         */
        default String propertyFile(){
            return "";
        }

        /** File checker, use to prevent unrelated files getting parsed into assets */
        boolean valid(Fi file);

        /** Method builder for asset loading */
        void load(MethodSpec.Builder builder);

        /** Default initializer for the static asset fields before they are loaded. */
        default CodeBlock initializer(){
            return CodeBlock.builder().add("new $T()", tName(type())).build();
        }
    }
}