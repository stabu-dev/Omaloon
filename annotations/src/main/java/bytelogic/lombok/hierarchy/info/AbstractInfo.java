package bytelogic.lombok.hierarchy.info;

import com.sun.tools.javac.code.Type;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class AbstractInfo {
    @NonNull
    @Getter
    final String name;
    @NonNull
    @Getter
    final String flatName;
    private final Map<InfoKey<?>, Object> infoMap = new HashMap<>();

    public AbstractInfo(Type type) {
        this(type.toString(),type.tsym.flatName().toString());
    }

    @Nullable
    public <T> T get(InfoKey<T> key) {
        //noinspection unchecked
        return (T) infoMap.get(key);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public <T> void put(InfoKey<T> key, T value) {
        infoMap.put(key, value);
    }
    public boolean has(InfoKey<?> key) {
        return infoMap.containsKey(key);
    }
}
