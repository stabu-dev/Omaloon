package omaloon.annotations.processors.util;

import arc.struct.*;

import com.squareup.javapoet.*;

import mindustry.io.*;

import omaloon.annotations.processors.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;

import static omaloon.annotations.processors.BaseProcessor.*;

/**
 * @author Anuke
 * @author GlennFolker
 */
public class TypeIOResolver{
    public static ClassSerializer resolve(BaseProcessor proc){
        ClassSerializer out = new ClassSerializer(new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>());

        TypeElement type = proc.elements.getTypeElement(TypeIO.class.getCanonicalName());
        Seq<ExecutableElement> methods = proc.methods(type);
        for(ExecutableElement meth : methods){
            if(proc.is(meth, Modifier.PUBLIC) && proc.is(meth, Modifier.STATIC)){
                Seq<VariableElement> params = Seq.with(meth.getParameters()).as();

                if(params.size == 2 && tName(params.first()).toString().equals("arc.util.io.Writes")){
                    ObjectMap<String, String> targetMap = simpleName(meth).endsWith("Net") ? out.netWriters : out.writers;
                    targetMap.put(fix(tName(params.get(1)).toString()), fullName(type) + "." + simpleName(meth));
                }else if(params.size == 1 && tName(params.first()).toString().equals("arc.util.io.Reads") && meth.getReturnType().getKind() != TypeKind.VOID){
                    ObjectMap<String, String> targetMap = simpleName(meth).endsWith("Net") ? out.netReaders : out.readers;
                    targetMap.put(fix(TypeName.get(meth.getReturnType()).toString()), fullName(type) + "." + simpleName(meth));
                }else if(params.size == 2 && tName(params.first()).toString().equals("arc.util.io.Reads") && meth.getReturnType().getKind() != TypeKind.VOID && proc.types.isSameType(meth.getReturnType(), meth.getParameters().get(1).asType())){
                    out.mutatorReaders.put(fix(TypeName.get(meth.getReturnType()).toString()), fullName(type) + "." + simpleName(meth));
                }
            }
        }

        return out;
    }

    private static String fix(String str){
        return str.replace("mindustry.gen", "").replace(BaseProcessor.sanitizedModName + ".gen", "");
    }

    /** Information about read/write methods for class types. */
    public static class ClassSerializer{
        public final ObjectMap<String, String> writers, readers, mutatorReaders, netWriters, netReaders;

        public ClassSerializer(ObjectMap<String, String> writers, ObjectMap<String, String> readers, ObjectMap<String, String> mutatorReaders, ObjectMap<String, String> netWriters, ObjectMap<String, String> netReaders){
            this.writers = writers;
            this.readers = readers;
            this.mutatorReaders = mutatorReaders;
            this.netWriters = netWriters;
            this.netReaders = netReaders;
        }

        public String getNetWriter(String type, String fallback){
            return netWriters.get(type, writers.get(type, fallback));
        }

        public String getNetReader(String type, String fallback){
            return netReaders.get(type, readers.get(type, fallback));
        }
    }
}
