package bytelogic.lombok.hierarchy.info;

import com.sun.tools.javac.code.Type;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class InterfaceInfo extends AbstractInfo {

    final Map<String, InterfaceInfo> subtypes = new HashMap<>();
    final Map<String, ClassInfo> impltypes = new HashMap<>();
    private final Map<InfoKey<?>, Object> infoMap = new HashMap<>();
    /**
     * interface It extends It1,It2,It2{
     *
     * }
     *
     * It.supertypes={
     * "It1":It1,
     * "It2":It2,
     * "It3":It3
     * }
     * */
    public Map<String, InterfaceInfo> supertypes = new HashMap<>();

    public InterfaceInfo(@NonNull String name, @NonNull String flatName) {
        super(name, flatName);
    }

    public InterfaceInfo(Type type) {
        super(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof InterfaceInfo info && name.equals(info.name);
    }


    public void visitSubtypes(Consumer<InterfaceInfo> visitor, boolean visitDeep, boolean visitSelf) {
        if (visitSelf) visitor.accept(this);
        if (!visitDeep) {
            subtypes.values().forEach(visitor);
            return;
        }
        for (InterfaceInfo info : subtypes.values()) {
            info.visitSubtypes(visitor, true, true);
        }
    }

    public void addSub(InterfaceInfo subInfo) {
        subtypes.put(subInfo.name, subInfo);
        subInfo.supertypes.put(name, this);
    }

    public void addImpl(ClassInfo info) {
        impltypes.put(info.name, info);
    }


}
