package bytelogic.lombok.hierarchy.info;

import com.sun.tools.javac.code.*;
import lombok.*;
import lombok.experimental.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;


@FieldDefaults(level = AccessLevel.PUBLIC)
public class ClassInfo extends AbstractInfo{
    final Map<String, ClassInfo> subtypes = new HashMap<>();

    @Nullable
    private ClassInfo parent;

    public ClassInfo(@NonNull String name, @NonNull String flatName){
        super(name, flatName);
    }

    public ClassInfo(Type type){
        super(type);
    }


    public ClassInfo parent(){
        return parent;
    }

    @Override
    public boolean equals(Object obj){
        return obj == this || obj instanceof ClassInfo info && info.name.equals(name);
    }


    public void visitSubtypes(Consumer<ClassInfo> visitor, boolean visitDeep, boolean visitSelf){
        if(visitSelf) visitor.accept(this);
        if(!visitDeep){
            subtypes.values().forEach(visitor);
            return;
        }
        for(ClassInfo info : subtypes.values()){
            info.visitSubtypes(visitor, true, true);
        }
    }

    public void addSub(ClassInfo subInfo){
        subtypes.put(subInfo.name, subInfo);
        subInfo.parent = this;
    }
}
